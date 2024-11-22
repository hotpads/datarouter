/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.gcp.bigtable.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bigtable.admin.v2.GcRule;
import com.google.cloud.bigtable.admin.v2.models.ColumnFamily;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import com.google.cloud.bigtable.admin.v2.models.GCRules;
import com.google.cloud.bigtable.admin.v2.models.GCRules.DurationRule;
import com.google.cloud.bigtable.admin.v2.models.GCRules.UnionRule;
import com.google.cloud.bigtable.admin.v2.models.GCRules.VersionRule;
import com.google.cloud.bigtable.admin.v2.models.ModifyColumnFamiliesRequest;
import com.google.cloud.bigtable.admin.v2.models.Table;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.type.DatarouterEmailTypes.SchemaUpdatesEmailType;
import io.datarouter.gcp.bigtable.config.BigtableClientsHolder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.model.serialize.fielder.TtlFielderConfig;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.retry.RetryableTool;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.settings.DatarouterSchemaUpdateEmailSettings;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.handler.EmailingSchemaUpdateService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class BigtableSchemaUpdateService extends EmailingSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(BigtableSchemaUpdateService.class);

	public static final String DEFAULT_FAMILY_QUALIFIER = "a";
	private static final int MAX_VERSIONS = 1;

	private final BigtableClientsHolder holder;
	private final SchemaUpdateOptions schemaUpdateOptions;

	@Inject
	public BigtableSchemaUpdateService(
			ServerName serverName,
			EnvironmentName environmentName,
			AdminEmail adminEmail,
			DatarouterSchemaUpdateScheduler executor,
			Provider<DatarouterClusterSchemaUpdateLockDao> schemaUpdateLockDao,
			Provider<ChangelogRecorder> changelogRecorder,
			String buildId,
			DatarouterHtmlEmailService htmlEmailService,
			BigtableClientsHolder holder,
			SchemaUpdateOptions schemaUpdateOptions,
			DatarouterWebPaths datarouterWebPaths,
			StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService,
			SchemaUpdatesEmailType schemaUpdatesEmailType,
			DatarouterSchemaUpdateEmailSettings schemaUpdateEmailSettings){
		super(
				serverName,
				environmentName,
				adminEmail,
				executor,
				schemaUpdateLockDao,
				changelogRecorder,
				buildId,
				htmlEmailService,
				datarouterWebPaths,
				standardDatarouterEmailHeaderService,
				schemaUpdatesEmailType,
				schemaUpdateEmailSettings);
		this.holder = holder;
		this.schemaUpdateOptions = schemaUpdateOptions;
	}

	@Override
	protected Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(ClientId clientId,
			Supplier<List<String>> existingTableNames, PhysicalNode<?,?,?> node){
		return () -> generateSchemaUpdate(clientId, existingTableNames, node);
	}

	@Override
	protected List<String> fetchExistingTables(ClientId clientId){
		return holder.getTableAdminClient(clientId).listTables();
	}

	private Optional<SchemaUpdateResult> generateSchemaUpdate(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node){
		PhysicalDatabeanFieldInfo<?,?,?> fieldInfo = node.getFieldInfo();
		String tableName = fieldInfo.getTableName();
		if(!existingTableNames.get().contains(tableName)){
			createTable(clientId, node);
			return Optional.empty();
		}
		// retry in case table is still being initialized by another thread
		Table table = RetryableTool.tryNTimesWithBackoffUnchecked(
				() -> holder.getTableAdminClient(clientId).getTable(tableName),
				5,
				1_000,
				false);
		Long requestedTtlSeconds = fieldInfo.getSampleFielder().getOption(TtlFielderConfig.KEY)
				.map(TtlFielderConfig::getTtl)
				.map(Duration::getSeconds)
				.orElse(null);
		List<String> ddls = new ArrayList<>();
		for(ColumnFamily family : table.getColumnFamilies()){
			String familyId = family.getId();
			GcRule gcRule = family.getGCRule().toProto();
			BigtableGcRules ruleSet = buildSetOfRules(gcRule);
			if(requestedTtlSeconds != null && !requestedTtlSeconds.equals(ruleSet.ttlSeconds)){
				String ddl = "alter '" + tableName + "', NAME => '" + familyId + "', TTL => " + requestedTtlSeconds;
				if(schemaUpdateOptions.getModifyTtl(false)){
					logger.warn(SchemaUpdateTool.generateFullWidthMessage("Executing SchemaUpdate"));
					logger.warn(ddl);
					UnionRule rule = buildGcRule(requestedTtlSeconds);
					ModifyColumnFamiliesRequest request = ModifyColumnFamiliesRequest.of(table.getId())
							.updateFamily(family.getId(), rule);
					holder.getTableAdminClient(clientId).modifyFamilies(request);
				}else if(schemaUpdateOptions.getModifyTtl(true)){
					SchemaUpdateTool.printSchemaUpdate(logger, clientId.getName(), clientId.getName(), tableName, ddl);
					ddls.add(ddl);
				}
			}else if(ruleSet.maxVersions == null || MAX_VERSIONS != ruleSet.maxVersions){
				String ddl = "alter '" + tableName + "', NAME => '" + familyId + "', VERSIONS => " + MAX_VERSIONS;
				if(schemaUpdateOptions.getModifyMaxVersions(false)){
					logger.warn(SchemaUpdateTool.generateFullWidthMessage("Executing SchemaUpdate"));
					logger.warn(ddl);
					UnionRule rule = buildGcRule(requestedTtlSeconds);
					ModifyColumnFamiliesRequest request = ModifyColumnFamiliesRequest.of(table.getId())
							.updateFamily(familyId, rule);
					holder.getTableAdminClient(clientId).modifyFamilies(request);
				}else if(schemaUpdateOptions.getModifyMaxVersions(true)){
					SchemaUpdateTool.printSchemaUpdate(logger, clientId.getName(), clientId.getName(), tableName, ddl);
					ddls.add(ddl);
				}
			}
		}
		if(ddls.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(new SchemaUpdateResult(String.join("\n", ddls), null, clientId));
	}

	private void createTable(ClientId clientId, PhysicalNode<?,?,?> node){
		String tableName = node.getFieldInfo().getTableName();
		DatabeanFielder<?,?> fielder = node.getFieldInfo().getSampleFielder();
		if(schemaUpdateOptions.getCreateTables(false)){
			logger.warn("table {} not found, creating it", tableName);
			try{
				Long requestedTtlSeconds = fielder.getOption(TtlFielderConfig.KEY)
						.map(TtlFielderConfig::getTtl)
						.map(Duration::getSeconds)
						.orElse(null);
				UnionRule rule = buildGcRule(requestedTtlSeconds);
				CreateTableRequest request = CreateTableRequest.of(tableName)
						.addFamily(DEFAULT_FAMILY_QUALIFIER, rule);
				holder.getTableAdminClient(clientId).createTable(request);
				logger.warn("created table {}", tableName);
			}catch(Exception e){
				logger.warn("unable to create table {}", tableName, e);
			}
		}else if(schemaUpdateOptions.getCreateTables(true)){
			logger.warn("table {} not found", tableName);
		}
	}

	private static UnionRule buildGcRule(Long ttlSeconds){
		UnionRule unionRule = GCRules.GCRULES.union();
		VersionRule versionRule = GCRules.GCRULES.maxVersions(MAX_VERSIONS);
		unionRule.rule(versionRule);
		if(ttlSeconds != null){
			DurationRule durationRule = GCRules.GCRULES.maxAge(ttlSeconds, TimeUnit.SECONDS);
			unionRule.rule(durationRule);
		}
		return unionRule;
	}

	private static BigtableGcRules buildSetOfRules(GcRule gcRule){
		var builder = new BigtableGcRulesBuilder();
		gcRule.getUnion().getRulesList().forEach(item -> {
			if(item.hasMaxAge()){
				builder.withTtlSeconds(item.getMaxAge().getSeconds());
			}
			if(item.hasMaxNumVersions()){
				builder.withMaxVersion(item.getMaxNumVersions());
			}
		});
		return builder.build();
	}

	private record BigtableGcRules(
			Long ttlSeconds,
			Integer maxVersions){
	}

	private static class BigtableGcRulesBuilder{
		private Long ttlSeconds;
		private Integer maxVersions;

		private BigtableGcRulesBuilder withTtlSeconds(Long ttlSeconds){
			this.ttlSeconds = ttlSeconds;
			return this;
		}

		private BigtableGcRulesBuilder withMaxVersion(Integer maxVersions){
			this.maxVersions = maxVersions;
			return this;
		}

		private BigtableGcRules build(){
			return new BigtableGcRules(ttlSeconds, maxVersions);
		}

	}

}

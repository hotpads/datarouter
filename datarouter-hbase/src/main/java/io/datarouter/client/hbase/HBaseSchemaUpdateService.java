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
package io.datarouter.client.hbase;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.client.HBaseConnectionHolder;
import io.datarouter.client.hbase.util.HBaseClientTool;
import io.datarouter.email.type.DatarouterEmailTypes.SchemaUpdatesEmailType;
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
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.settings.DatarouterSchemaUpdateEmailSettings;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.handler.EmailingSchemaUpdateService;
import io.datarouter.web.monitoring.BuildProperties;

@Singleton
public class HBaseSchemaUpdateService extends EmailingSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(HBaseSchemaUpdateService.class);

	// default table configuration settings for new tables
	// cast to long before overflowing int
	private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 1L * 4 * 1024 * 1024 * 1024;
	private static final long DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES = 1L * 256 * 1024 * 1024;
	private static final int MAX_VERSIONS = 1;

	private final HBaseConnectionHolder hBaseConnectionHolder;
	private final SchemaUpdateOptions schemaUpdateOptions;

	@Inject
	public HBaseSchemaUpdateService(
			ServerName serverName,
			EnvironmentName environmentName,
			AdminEmail adminEmail,
			DatarouterSchemaUpdateScheduler executor,
			DatarouterHtmlEmailService htmlEmailService,
			HBaseConnectionHolder hBaseConnectionHolder,
			SchemaUpdateOptions schemaUpdateOptions,
			DatarouterWebPaths datarouterWebPaths,
			Provider<DatarouterClusterSchemaUpdateLockDao> schemaUpdateLockDao,
			Provider<ChangelogRecorder> changelogRecorder,
			BuildProperties buildProperties,
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
				buildProperties.getBuildId(),
				htmlEmailService,
				datarouterWebPaths,
				standardDatarouterEmailHeaderService,
				schemaUpdatesEmailType,
				schemaUpdateEmailSettings);
		this.hBaseConnectionHolder = hBaseConnectionHolder;
		this.schemaUpdateOptions = schemaUpdateOptions;
	}

	@Override
	protected Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node){
		return () -> generateSchemaUpdate(clientId, existingTableNames, node);
	}


	@Override
	protected List<String> fetchExistingTables(ClientId clientId){
		TableName[] tableNames;
		try{
			tableNames = hBaseConnectionHolder.getConnection(clientId).getAdmin().listTableNames();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return Arrays.stream(tableNames)
				.map(TableName::getNameAsString)
				.collect(Collectors.toList());
	}

	private Optional<SchemaUpdateResult> generateSchemaUpdate(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node)
	throws IOException{
		PhysicalDatabeanFieldInfo<?,?,?> fieldInfo = node.getFieldInfo();
		TableName tableName = TableName.valueOf(fieldInfo.getTableName());
		if(!existingTableNames.get().contains(tableName.getNameAsString())){
			createTable(clientId, node);
			return Optional.empty();
		}

		Admin admin = hBaseConnectionHolder.getConnection(clientId).getAdmin();
		admin.getAlterStatus(tableName);

		HTableDescriptor desc = admin.getTableDescriptor(tableName);

		int requestedTtlSeconds = fieldInfo.getSampleFielder().getOption(TtlFielderConfig.KEY)
				.map(TtlFielderConfig::getTtl)
				.map(Duration::getSeconds)
				.map(Math::toIntExact)
				.orElse(HConstants.FOREVER);
		List<String> ddls = new ArrayList<>();
		for(HColumnDescriptor column : desc.getColumnFamilies()){
			if(requestedTtlSeconds != column.getTimeToLive()){
				String ddl = "alter '" + tableName + "', NAME => '" + column.getNameAsString() + "', TTL => "
						+ requestedTtlSeconds;
				if(schemaUpdateOptions.getModifyTtl(false)){
					logger.warn(SchemaUpdateTool.generateFullWidthMessage("Executing SchemaUpdate"));
					logger.warn(ddl);
					column.setTimeToLive(requestedTtlSeconds);
					admin.modifyColumn(tableName, column);
				}else if(schemaUpdateOptions.getModifyTtl(true)){
					logger.warn(SchemaUpdateTool.PLEASE_EXECUTE_SCHEMA_UPDATE_MESSAGE);
					logger.warn(ddl);
					logger.warn(SchemaUpdateTool.THANK_YOU_MESSAGE);
					ddls.add(ddl);
				}
			}
			if(MAX_VERSIONS != column.getMaxVersions()){
				String ddl = "alter '" + tableName + "', NAME => '" + column.getNameAsString() + "', VERSIONS => "
						+ MAX_VERSIONS;
				if(schemaUpdateOptions.getModifyMaxVersions(false)){
					logger.warn(SchemaUpdateTool.generateFullWidthMessage("Executing SchemaUpdate"));
					logger.warn(ddl);
					column.setMaxVersions(MAX_VERSIONS);
					admin.modifyColumn(tableName, column);
				}else if(schemaUpdateOptions.getModifyMaxVersions(true)){
					logger.warn(SchemaUpdateTool.PLEASE_EXECUTE_SCHEMA_UPDATE_MESSAGE);
					logger.warn(ddl);
					logger.warn(SchemaUpdateTool.THANK_YOU_MESSAGE);
					ddls.add(ddl);
				}
			}
		}
		if(ddls.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(new SchemaUpdateResult(String.join("\n", ddls), null, clientId));
	}

	private void createTable(ClientId clientId, PhysicalNode<?,?,?> node) throws IOException{
		String tableName = node.getFieldInfo().getTableName();
		if(schemaUpdateOptions.getCreateTables(false)){
			logger.warn("table " + tableName + " not found, creating it");
			try{
				HTableDescriptor htable = new HTableDescriptor(TableName.valueOf(tableName));
				htable.setMaxFileSize(DEFAULT_MAX_FILE_SIZE_BYTES);
				htable.setMemStoreFlushSize(DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES);
				HColumnDescriptor family = new HColumnDescriptor(HBaseClientManager.DEFAULT_FAMILY_QUALIFIER);
				DatabeanFieldInfo<?,?,?> fieldInfo = node.getFieldInfo();
				DatabeanFielder<?,?> fielder = fieldInfo.getSampleFielder();
				family.setMaxVersions(MAX_VERSIONS);
				family.setBloomFilterType(BloomType.NONE);
				family.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
				family.setCompressionType(Algorithm.GZ);
				int ttlSeconds = fielder.getOption(TtlFielderConfig.KEY)
						.map(TtlFielderConfig::getTtl)
						.map(Duration::getSeconds)
						.map(Math::toIntExact)
						.orElse(HConstants.FOREVER);
				family.setTimeToLive(ttlSeconds);
				htable.addFamily(family);
				byte[][] splitPoints = HBaseClientTool.getSplitPoints(node);
				Admin admin = hBaseConnectionHolder.getConnection(clientId).getAdmin();
				if(ArrayTool.isEmpty(splitPoints) || ArrayTool.isEmpty(splitPoints[0])){// a single empty byte array
					admin.createTable(htable);
				}else{
					// careful, as throwing strange split points in here can crash master
					// and corrupt meta table
					admin.createTable(htable, splitPoints);
				}
				logger.warn("created table " + tableName);
			}catch(TableExistsException e){
				logger.warn("table " + tableName + " already created by another process");
			}
		}else if(schemaUpdateOptions.getCreateTables(true)){
			logger.warn("table " + tableName + " not found");
		}
	}

}

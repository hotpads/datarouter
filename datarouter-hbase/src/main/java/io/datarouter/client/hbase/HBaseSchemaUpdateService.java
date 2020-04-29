/**
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

import static j2html.TagCreator.pre;

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
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.model.serialize.fielder.TtlFielderConfig;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.executor.DatarouterStorageExecutors.DatarouterSchemaUpdateScheduler;
import io.datarouter.storage.config.schema.BaseSchemaUpdateService;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.email.DatarouterHtmlEmailService;

@Singleton
public class HBaseSchemaUpdateService extends BaseSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(HBaseSchemaUpdateService.class);

	// default table configuration settings for new tables
	// cast to long before overflowing int
	private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 1L * 4 * 1024 * 1024 * 1024;
	private static final long DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES = 1L * 256 * 1024 * 1024;
	private static final int MAX_VERSIONS = 1;

	private final DatarouterHtmlEmailService htmlEmailService;
	private final HBaseConnectionHolder hBaseConnectionHolder;
	private final SchemaUpdateOptions schemaUpdateOptions;
	private final DatarouterWebPaths datarouterWebPaths;

	@Inject
	public HBaseSchemaUpdateService(DatarouterProperties datarouterProperties,
			DatarouterAdministratorEmailService adminEmailService,
			DatarouterSchemaUpdateScheduler executor,
			DatarouterHtmlEmailService htmlEmailService,
			HBaseConnectionHolder hBaseConnectionHolder,
			SchemaUpdateOptions schemaUpdateOptions,
			DatarouterWebPaths datarouterWebPaths){
		super(datarouterProperties, adminEmailService, executor);
		this.htmlEmailService = htmlEmailService;
		this.hBaseConnectionHolder = hBaseConnectionHolder;
		this.schemaUpdateOptions = schemaUpdateOptions;
		this.datarouterWebPaths = datarouterWebPaths;
	}

	@Override
	protected Callable<Optional<SchemaUpdateResult>> makeSchemaUpdateCallable(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> node){
		return () -> generateSchemaUpdate(clientId, existingTableNames, node);
	}

	@Override
	protected void sendEmail(String fromEmail, String toEmail, String subject, String body){
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(datarouterWebPaths.datarouter)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(subject)
				.withTitle("HBase Schema Update")
				.withTitleHref(primaryHref)
				.withContent(pre(body));
		htmlEmailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
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
					logger.warn(SchemaUpdateTool.generateFullWidthMessage("Please Execute SchemaUpdate"));
					logger.warn(ddl);
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
					logger.warn(SchemaUpdateTool.generateFullWidthMessage("Please Execute SchemaUpdate"));
					logger.warn(ddl);
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

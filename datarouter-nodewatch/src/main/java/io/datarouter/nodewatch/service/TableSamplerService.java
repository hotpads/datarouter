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
package io.datarouter.nodewatch.service;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.TableSample;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.nodewatch.util.TableSamplerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.op.raw.write.SortedStorageWriter;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.storage.node.tableconfig.NodewatchConfiguration;
import io.datarouter.storage.node.tableconfig.NodewatchConfigurationBuilder;
import io.datarouter.storage.node.tableconfig.TableConfigurationService;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.tuple.Range;

@Singleton
public class TableSamplerService{
	private static final Logger logger = LoggerFactory.getLogger(TableSamplerService.class);

	public static final long COUNT_TIME_MS_SLOW_SPAN_THRESHOLD = Duration.ofMinutes(5).toMillis();

	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterClients clients;
	@Inject
	private NodewatchClientConfiguration nodewatchClientConfiguration;
	@Inject
	private DatarouterTableSampleDao tableSampleDao;
	@Inject
	private TableConfigurationService tableConfigurationService;

	public boolean isCountableNode(PhysicalNode<?,?,?> physicalNode){
		boolean isCountableTable = isCountableTable(new ClientTableEntityPrefixNameWrapper(physicalNode));
		if(!isCountableTable){
			return false;
		}
		boolean isCountableClient = nodewatchClientConfiguration.isCountableClient(physicalNode.getFieldInfo()
				.getClientId());
		if(!isCountableClient){
			return false;
		}
		//Can't scan a non-sorted node
		boolean isSortedStorageWriter = physicalNode instanceof SortedStorageWriter;
		if(!isSortedStorageWriter){
			return false;
		}
		//Strings will be case-sensitive, which is good for the scanners
		boolean hasBinaryCollation = MysqlTableOptions.make(physicalNode.getFieldInfo().getSampleFielder())
				.getCollation().isBinary();
		if(hasBinaryCollation){
			return true;
		}
		//Table is case-insensitive at this point.
		// Don't attempt scanning it if PK contains possibly case-insensitive fields
		boolean hasPossiblyCaseInsensitivePkFields = physicalNode.getFieldInfo().getPrimaryKeyFields().stream()
				.map(Field::getKey)
				.anyMatch(FieldKey::isPossiblyCaseInsensitive);
		return !hasPossiblyCaseInsensitivePkFields;
	}

	public Scanner<PhysicalSortedStorageReaderNode<?,?,?>> scanCountableNodes(){
		return Scanner.of(datarouterNodes.getWritableNodes(clients.getClientIds()))
				.include(this::isCountableNode)
				.map(PhysicalSortedStorageReaderNode.class::cast);
	}

	public Scanner<PhysicalSortedStorageReaderNode<?,?,?>> scanAllSortedMapStorageNodes(){
		return Scanner.of(datarouterNodes.getWritableAndReadableNodes(clients.getClientIds()))
				.include(node -> isCountableNode(node) || node instanceof SortedStorageReader)
				.map(PhysicalSortedStorageReaderNode.class::cast);
	}

	public boolean isCountableTable(ClientTableEntityPrefixNameWrapper clientWrapper){
		if(tableConfigurationService.getTableConfigMap().containsKey(clientWrapper)){
			return tableConfigurationService.getTableConfigMap().get(clientWrapper).isCountable;
		}
		return true;
	}

	public Scanner<TableSample> scanSamplesForNode(PhysicalNode<?,?,?> node){
		String tableName = node.getFieldInfo().getTableName();
		return node.getClientIds().stream()
				.map(ClientId::getName)
				.map(clientName -> new TableSampleKey(clientName, tableName, null, null))
				.map(tableSampleDao::scanWithPrefix)
				.findAny()
				.orElse(Scanner.empty());
	}

	public <PK extends PrimaryKey<PK>> Scanner<PK> scanPksForNode(PhysicalNode<PK,?,?> node){
		return scanSamplesForNode(node)
				.map(TableSample::getKey)
				.map(key -> TableSamplerTool.extractPrimaryKeyFromSampleKey(node, key));
	}

	public int getSampleInterval(PhysicalSortedStorageReaderNode<?,?,?> node){
		NodewatchConfiguration nodeConfig = tableConfigurationService.getTableConfigMap()
				.get(new ClientTableEntityPrefixNameWrapper(node));
		return nodeConfig == null ? NodewatchConfigurationBuilder.DEFAULT_SAMPLE_SIZE : nodeConfig.sampleSize;
	}

	public int getBatchSize(PhysicalSortedStorageReaderNode<?,?,?> node){
		NodewatchConfiguration nodeConfig = tableConfigurationService.getTableConfigMap()
				.get(new ClientTableEntityPrefixNameWrapper(node));
		return nodeConfig == null ? NodewatchConfigurationBuilder.DEFAULT_BATCH_SIZE : nodeConfig.batchSize;
	}

	public TableCount getCurrentTableCountFromSamples(String clientName, String tableName){
		//not distinguishing sub-entities at the moment
		TableSampleKey clientTablePrefix = new TableSampleKey(clientName, tableName, null, null);
		long totalRows = 0;
		long totalCountTimeMs = 0;
		long numSpans = 0;
		long numSlowSpans = 0;
		for(TableSample sample : tableSampleDao.scanWithPrefix(clientTablePrefix).iterable()){
			totalRows += sample.getNumRows();
			totalCountTimeMs += sample.getCountTimeMs();
			numSpans++;
			if(sample.getCountTimeMs() > COUNT_TIME_MS_SLOW_SPAN_THRESHOLD){
				numSlowSpans++;
			}
		}
		logger.info("total of {} rows for {}.{}", totalRows, clientName, tableName);
		return new TableCount(clientName, tableName, System.currentTimeMillis(), totalRows, totalCountTimeMs,
				numSpans, numSlowSpans);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Scanner<Range<PK>> scanTableRangesUsingTableSamples(PhysicalNode<PK,D,F> node){
		return Scanner.concat(scanPksForNode(node), Scanner.of((PK)null))
				.retain(1)
				.map(group -> new Range<>(group.previous(), group.current()));
	}

}

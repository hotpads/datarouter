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
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.op.raw.write.SortedStorageWriter;
import io.datarouter.storage.node.tableconfig.NodewatchConfigurationBuilder;
import io.datarouter.storage.node.tableconfig.TableConfigurationService;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.types.MilliTime;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TableSamplerService{

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

	/*---------- find countable clients ----------*/

	public List<ClientId> listClientIdsWithCountableNodes(){
		return scanCountableNodes()
				.map(PhysicalNode::getFieldInfo)
				.map(PhysicalDatabeanFieldInfo::getClientId)
				.distinct()
				.list();
	}

	/*---------- find countable nodes ----------*/

	public boolean isCountableNode(PhysicalNode<?,?,?> physicalNode){
		ClientId clientId = physicalNode.getClientId();
		boolean isCountableTable = isCountingEnabled(physicalNode);
		if(!isCountableTable){
			return false;
		}
		boolean isCountableClient = nodewatchClientConfiguration.isCountableClient(clientId);
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
				.include(SortedStorageReader.class::isInstance)
				.map(PhysicalSortedStorageReaderNode.class::cast);
	}

	/*--------- node configurations -----------*/

	public boolean isCountingEnabled(PhysicalNode<?,?,?> node){
		return tableConfigurationService.findConfig(node)
				.map(nodeConfig -> nodeConfig.isCountable)
				.orElse(NodewatchConfigurationBuilder.DEFAULT_ENABLED);
	}

	public int getSampleInterval(PhysicalNode<?,?,?> node){
		return tableConfigurationService.findConfig(node)
				.map(nodeConfig -> nodeConfig.sampleSize)
				.orElse(NodewatchConfigurationBuilder.DEFAULT_SAMPLE_SIZE);
	}

	public int getBatchSize(PhysicalNode<?,?,?> node){
		return tableConfigurationService.findConfig(node)
				.map(nodeConfig -> nodeConfig.batchSize)
				.orElse(NodewatchConfigurationBuilder.DEFAULT_BATCH_SIZE);
	}

	/*---------- fetch TableCount info -----------*/

	public TableCount getCurrentTableCountFromSamples(ClientAndTableNames clientAndTableNames){
		return getCurrentTableCountFromSamples(clientAndTableNames.client(), clientAndTableNames.table());
	}

	public TableCount getCurrentTableCountFromSamples(String clientName, String tableName){
		//not distinguishing sub-entities at the moment
		var clientTablePrefix = TableSampleKey.prefix(clientName, tableName);
		var totalRows = new AtomicLong();
		var totalCountTimeMs = new AtomicLong();
		var numSpans = new AtomicLong();
		var numSlowSpans = new AtomicLong();
		tableSampleDao.scanWithPrefix(clientTablePrefix)
				.forEach(sample -> {
					totalRows.addAndGet(sample.getNumRows());
					totalCountTimeMs.addAndGet(sample.getCountTimeMs());
					numSpans.incrementAndGet();
					if(sample.getCountTimeMs() > COUNT_TIME_MS_SLOW_SPAN_THRESHOLD){
						numSlowSpans.incrementAndGet();
					}
				});
		return new TableCount(
				clientName,
				tableName,
				MilliTime.now(),
				totalRows.get(),
				totalCountTimeMs.get(),
				numSpans.get(),
				numSlowSpans.get());
	}

	/*-------- scan samples -----------*/

	public Scanner<TableSample> scanSamplesForNode(PhysicalNode<?,?,?> node){
		var prefix = TableSampleKey.prefix(
				node.getClientId().getName(),
				node.getFieldInfo().getTableName());
		return tableSampleDao.scanWithPrefix(prefix);
	}

	public <PK extends PrimaryKey<PK>> boolean checkAllSamplesParseable(PhysicalNode<PK,?,?> node){
		return scanSamplesForNode(node)
				.map(TableSample::getKey)
				.allMatch(key -> TableSamplerTool.checkIsParseableSampleKey(node, key));
	}

	public <PK extends PrimaryKey<PK>> Scanner<PK> scanPksForNode(PhysicalNode<PK,?,?> node){
		return scanSamplesForNode(node)
				.map(TableSample::getKey)
				.map(key -> TableSamplerTool.extractPrimaryKeyFromSampleKey(node, key));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Scanner<Range<PK>> scanTableRangesUsingTableSamples(PhysicalNode<PK,D,F> node){
		return Scanner.concat(scanPksForNode(node), Scanner.of((PK)null))
				.retain(1)
				.map(group -> new Range<>(group.previous(), group.current()));
	}

	/*-------- scan samples grouped by countTimeMs -----------*/

	public Scanner<TableSample> scanSampledSamplesByCountingTime(
			PhysicalNode<?,?,?> node,
			Duration minCountingTime){
		var prefix = TableSampleKey.prefix(
				node.getClientId().getName(),
				node.getFieldInfo().getTableName());
		return tableSampleDao.scanWithPrefix(prefix)
				.batchByMinSize(minCountingTime.toMillis(), TableSample::getCountTimeMs)
				.map(List::getLast);
	}

	public <PK extends PrimaryKey<PK>> Scanner<PK> scanSampledPksByCountingTime(
			PhysicalNode<PK,?,?> node,
			Duration minCountingTime){
		return scanSampledSamplesByCountingTime(node, minCountingTime)
				.map(TableSample::getKey)
				.map(key -> TableSamplerTool.extractPrimaryKeyFromSampleKey(node, key));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Scanner<Range<PK>> scanSampledPkRangesByCountingTime(
			PhysicalNode<PK,D,F> node,
			Duration minCountingTime){
		return Scanner.concat(scanSampledPksByCountingTime(node, minCountingTime), Scanner.of((PK)null))
				.retain(1)
				.map(group -> new Range<>(group.previous(), group.current()));
	}

	/*-------- scan data -----------*/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Scanner<List<PK>> scanKeyBatchesParallelUnordered(
			Threads threads,
			PhysicalSortedStorageNode<PK,D,F> node,
			int batchSize){
		var config = new Config().setResponseBatchSize(batchSize);
		return scanTableRangesUsingTableSamples(node)
				.merge(threads, range -> node.scanKeys(range, config).batch(batchSize));
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Scanner<List<D>> scanBatchesParallelUnordered(
			Threads threads,
			PhysicalSortedStorageNode<PK,D,F> node,
			int batchSize){
		var config = new Config().setResponseBatchSize(batchSize);
		return scanTableRangesUsingTableSamples(node)
				.merge(threads, range -> node.scan(range, config).batch(batchSize));
	}

}

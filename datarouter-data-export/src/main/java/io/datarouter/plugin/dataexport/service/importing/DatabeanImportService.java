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
package io.datarouter.plugin.dataexport.service.importing;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.KvString;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.bytes.kvfile.io.read.KvFileReader;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.util.PhysicalSortedNodeWrapper;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanImportPutMultiExecutor;
import io.datarouter.plugin.dataexport.service.blockfile.DatabeanExportKvFileService;
import io.datarouter.plugin.dataexport.service.blockfile.DatabeanExportKvFileStorageService;
import io.datarouter.plugin.dataexport.util.DatabeanExportFilenameTool;
import io.datarouter.plugin.dataexport.util.RateTracker;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import io.datarouter.storage.util.Subpath;
import io.datarouter.types.Ulid;
import io.datarouter.util.Count;
import io.datarouter.util.Count.Counts;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.number.NumberFormatter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatabeanImportService{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanImportService.class);

	private static final Duration LOG_PERIOD = Duration.ofSeconds(5);
	private static final int PUT_MULTI_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
	private static final int PUT_BATCH_SIZE = 100;

	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatabeanExportKvFileStorageService kvFileStorageService;
	@Inject
	private DatabeanExportKvFileService kvFileService;
	@Inject
	private DatabeanImportPutMultiExecutor putMultiExec;

	public record DatabeanImportResponse(
			long totalDatabeans,
			List<String> nodeNames){
	}

	public DatabeanImportResponse importAllTables(Ulid exportId){
		var totalDatabeans = new AtomicLong(0);
		Directory metaDirectory = kvFileStorageService.makeExportMetaDirectory(exportId);
		List<String> nodeNames = metaDirectory.scanKeys(Subpath.empty())
				.map(PathbeanKey::getFile)
				.map(DatabeanExportFilenameTool::parseClientAndTableName)
				.map(clientAndTableName -> datarouterNodes.getPhysicalNodeForClientAndTable(
						clientAndTableName.clientName(),
						clientAndTableName.tableName()))
				.map(Node::getName)
				.each(nodeName -> {
					long numDatabeans = importTable(exportId, nodeName);
					totalDatabeans.addAndGet(numDatabeans);
				})
				.list();
		return new DatabeanImportResponse(totalDatabeans.get(), nodeNames);
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long importTable(
			Ulid exportId,
			String nodeName){
		var nodeWrapper = new PhysicalSortedNodeWrapper<PK,D,F>(datarouterNodes, nodeName);
		Directory tableDataDirectory = kvFileStorageService.makeTableDataDirectory(exportId, nodeWrapper.node);
		return tableDataDirectory.scanKeys(Subpath.empty())
				.map(DatabeanExportFilenameTool::partId)
				.map(partId -> importPart(exportId, nodeName, partId))
				.reduce(0L, Long::sum);
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long importPart(
			Ulid exportId,
			String nodeName,
			int partId){
		var nodeWrapper = new PhysicalSortedNodeWrapper<PK,D,F>(datarouterNodes, nodeName);
		BlockfileStorage blockfileTableStorage = kvFileStorageService.makeTableDataStorage(
				exportId,
				nodeWrapper.node);
		var kvFileReaderReader = kvFileService.makeKvFileReader(
				blockfileTableStorage,
				nodeWrapper.node,
				partId);
		return importFromBlockfile(
				kvFileReaderReader,
				exportId,
				nodeWrapper.node,
				PUT_BATCH_SIZE);
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long importFromBlockfile(
			KvFileReader<?> untypedKvFileReader,
			Ulid exportId,
			PhysicalSortedMapStorageNode<PK,D,F> node,
			int putBatchSize){
		logger.warn("importing {}", node.getName());
		@SuppressWarnings("unchecked")
		var blockfileReader = (KvFileReader<D>)untypedKvFileReader;
		var counts = new Counts();
		var numDatabeans = counts.add("numDatabeans");
		var lastKey = new AtomicReference<PK>();
		var rateTracker = new RateTracker();
		blockfileReader.scan()
				.batch(putBatchSize)
				.parallelUnordered(new Threads(putMultiExec, PUT_MULTI_THREADS))
				.each(node::putMulti)
				.each(batch -> lastKey.set(ListTool.getLast(batch).getKey()))
				.each(numDatabeans::incrementBySize)
				.each(rateTracker::incrementBySize)
				.periodic(LOG_PERIOD, batch -> logProgress(
						exportId,
						node.getName(),
						numDatabeans,
						rateTracker,
						lastKey.get().toString()))
				.count();
		logProgress(
				exportId,
				node.getName(),
				numDatabeans,
				rateTracker,
				"[end]");
		return numDatabeans.value();
	}

	private void logProgress(
			Ulid exportId,
			String nodeName,
			Count numDatabeans,
			RateTracker rateTracker,
			String through){
		String message = "imported " + new KvString()
				.add("databeans", numDatabeans.value(), NumberFormatter::addCommas)
				.add("perSec", rateTracker.perSecDisplay())
				.add("perSecAvg", rateTracker.perSecAvgDisplay())
				.add("exportId", exportId, Ulid::toString)
				.add("node", nodeName)
				.add("through", through);
		logger.warn(message);
		rateTracker.markLogged();
	}

}

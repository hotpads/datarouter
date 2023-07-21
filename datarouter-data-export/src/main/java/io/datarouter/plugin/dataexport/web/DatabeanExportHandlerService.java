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
package io.datarouter.plugin.dataexport.web;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.util.PhysicalSortedNodeWrapper;
import io.datarouter.plugin.dataexport.service.exporting.DatabeanExportService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.SortedStorageNode;
import io.datarouter.types.Ulid;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatabeanExportHandlerService{

	public static final int DEFAULT_TABLE_SAMPLES_PER_PART = 3;

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatabeanExportService databeanExportService;

	public Scanner<String> scanPossibleNodeNames(){
		return Scanner.of(datarouterNodes.getWritableNodes(datarouterClients.getClientIds()))
				.include(node -> node instanceof SortedStorageNode)
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName());
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportNode(
			Ulid exportId,
			String nodeName,
			Optional<String> optStartKeyInclusiveString,
			Optional<String> optEndKeyExclusiveString,
			Optional<String> optMaxRowsString,
			Optional<String> optScanBatchSizeString,
			Optional<String> optNumThreadsString,
			boolean parallel){
		var typedNodeWrapper = new PhysicalSortedNodeWrapper<PK,D,F>(datarouterNodes, nodeName);
		Range<PK> keyRange = new Range<>(
				optStartKeyInclusiveString.orElse(null),
				true,
				optEndKeyExclusiveString.orElse(null),
				false)
				.map(typedNodeWrapper::parsePk);
		long maxRows = optMaxRowsString
				.map(Long::valueOf)
				.orElse(Long.MAX_VALUE);
		int scanBatchSize = optScanBatchSizeString
				.map(Integer::valueOf)
				.orElse(DatabeanExportHandler.DEFAULT_SCAN_BATCH_SIZE);
		int numThreads = optNumThreadsString
				.map(Integer::valueOf)
				.orElse(DatabeanExportHandler.DEFAULT_NUM_THREADS);
		return databeanExportService.exportNode(
				exportId,
				typedNodeWrapper.node,
				keyRange,
				maxRows,
				scanBatchSize,
				DEFAULT_TABLE_SAMPLES_PER_PART,
				numThreads,
				parallel);
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportNodes(
			Ulid exportId,
			List<String> nodeNames,
			Optional<String> optMaxRowsString,
			Optional<String> optScanBatchSizeString){
		long maxRows = optMaxRowsString
				.map(Long::valueOf)
				.orElse(Long.MAX_VALUE);
		int scanBatchSize = optScanBatchSizeString
				.map(Integer::valueOf)
				.orElse(DatabeanExportHandler.DEFAULT_SCAN_BATCH_SIZE);
		var totalDatabeans = new AtomicLong();
		nodeNames.forEach(nodeName -> {
			var typedNodeWrapper = new PhysicalSortedNodeWrapper<PK,D,F>(datarouterNodes, nodeName);
			long numDatabeans = databeanExportService.exportNode(
					exportId,
					typedNodeWrapper.node,
					Range.everything(),
					maxRows,
					scanBatchSize,
					DEFAULT_TABLE_SAMPLES_PER_PART,
					1,// ignored
					false);
			totalDatabeans.addAndGet(numDatabeans);
		});
		return totalDatabeans.get();
	}

}

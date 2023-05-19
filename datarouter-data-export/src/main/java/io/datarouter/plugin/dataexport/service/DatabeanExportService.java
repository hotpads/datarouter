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
package io.datarouter.plugin.dataexport.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportDirectorySupplier;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanExportPrefetchExecutor;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanExportWriteParallelExecutor;
import io.datarouter.plugin.dataexport.service.DatabeanExportToDirectory.DatabeanExportToDirectoryRequest;
import io.datarouter.plugin.dataexport.web.TypedNodeWrapper;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.SortedStorageNode;
import io.datarouter.util.tuple.Range;

@Singleton
public class DatabeanExportService{

	public static final int DEFAULT_SCAN_BATCH_SIZE = Config.DEFAULT_REQUEST_BATCH_SIZE;
	private static final int NUM_UPLOAD_THREADS = 4;

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterDataExportDirectorySupplier directorySupplier;
	@Inject
	private DatabeanExportPrefetchExecutor databeanExportPrefetchExec;
	@Inject
	private DatabeanExportWriteParallelExecutor databeanExportWriteParallelExec;

	public Scanner<String> scanPossibleNodeNames(){
		return Scanner.of(datarouterNodes.getWritableNodes(datarouterClients.getClientIds()))
				.include(node -> node instanceof SortedStorageNode)
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName());
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportNodeFromHandler(
			String exportId,
			String nodeName,
			Optional<String> optStartKeyInclusiveString,
			Optional<String> optEndKeyExclusiveString,
			Optional<String> optMaxRowsString,
			Optional<String> optScanBatchSizeString){
		var typedNodeWrapper = new TypedNodeWrapper<PK,D,F>(datarouterNodes, nodeName);
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
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		return exportNode(
				exportId,
				typedNodeWrapper.node,
				keyRange,
				maxRows,
				scanBatchSize);
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportNodesFromHandler(
			String exportId,
			List<String> nodeNames,
			Optional<String> optMaxRowsString,
			Optional<String> optScanBatchSizeString){
		long maxRows = optMaxRowsString
				.map(Long::valueOf)
				.orElse(Long.MAX_VALUE);
		int scanBatchSize = optScanBatchSizeString
				.map(Integer::valueOf)
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		var totalDatabeans = new AtomicLong();
		nodeNames.forEach(nodeName -> {
			var typedNodeWrapper = new TypedNodeWrapper<PK,D,F>(datarouterNodes, nodeName);
			long numDatabeans = exportNode(
					exportId,
					typedNodeWrapper.node,
					Range.everything(),
					maxRows,
					scanBatchSize);
			totalDatabeans.addAndGet(numDatabeans);
		});
		return totalDatabeans.get();
	}

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportNode(
			String exportId,
			SortedStorageNode<PK,D,F> node,
			Range<PK> keyRange,
			long maxRows,
			int scanBatchSize){
		var databeanExportToDirectoryRequest = new DatabeanExportToDirectoryRequest<>(
				directorySupplier.getDirectory(),
				exportId,
				node,
				keyRange,
				maxRows,
				scanBatchSize,
				databeanExportPrefetchExec,
				new Threads(databeanExportWriteParallelExec, NUM_UPLOAD_THREADS));
		return new DatabeanExportToDirectory<>(databeanExportToDirectoryRequest).execute();
	}

}

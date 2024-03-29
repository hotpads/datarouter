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
package io.datarouter.plugin.dataexport.service.exporting;

import java.util.concurrent.ExecutorService;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanExportParallelPartsExecutor;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanExportPrefetchExecutor;
import io.datarouter.plugin.dataexport.service.blockfile.DatabeanExportBlockfileService;
import io.datarouter.plugin.dataexport.service.blockfile.DatabeanExportBlockfileStorageService;
import io.datarouter.plugin.dataexport.util.DatabeanExportFilenameTool;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.types.Ulid;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatabeanExportService{

	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private DatabeanExportParallelPartsExecutor parallelPartsExec;
	@Inject
	private DatabeanExportPrefetchExecutor databeanExportPrefetchExec;
	@Inject
	private DatabeanExportBlockfileStorageService blockfileStorageService;
	@Inject
	private DatabeanExportBlockfileService blockfileService;

	public record DatabeanExportRequest<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>(
		Directory tableDirectory,
		DatabeanExportBlockfileService blockfileService,
		Ulid exportId,
		PhysicalSortedStorageReaderNode<PK,D,F> node,
		TableSamplerService tableSamplerService,
		Range<PK> pkRange,
		long maxRows,
		Threads partThreads,
		ExecutorService prefetchExec,
		int scanBatchSize,
		int tableSamplesPerPart){
	}

	public record DatabeanExportResponse(
			String nodeName,
			int numParts,
			long numDatabeans){
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportNode(
			Ulid exportId,
			PhysicalSortedStorageNode<PK,D,F> node,
			Range<PK> keyRange,
			long maxRows,
			int scanBatchSize,
			int tableSamplesPerPart,
			int numThreads,
			boolean parallel){
		Directory tableDataDirectory = blockfileStorageService.makeTableDataDirectory(exportId, node);
		var exportRequest = new DatabeanExportRequest<>(
				tableDataDirectory,
				blockfileService,
				exportId,
				node,
				tableSamplerService,
				keyRange,
				maxRows,
				new Threads(parallelPartsExec, numThreads),
				databeanExportPrefetchExec,
				scanBatchSize,
				tableSamplesPerPart);
		DatabeanExportResponse response = parallel
				? new ParallelDatabeanExport<>(exportRequest).exportTable()
				: new DatabeanExport<>(exportRequest).exportTable();
		writeMetadata(exportId, node, response);
		return response.numDatabeans();
	}

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void writeMetadata(
			Ulid exportId,
			PhysicalSortedStorageNode<PK,D,F> node,
			DatabeanExportResponse exportResponse){
		var key = PathbeanKey.of(DatabeanExportFilenameTool.makeClientAndTableName(node));
		var value = new BinaryDictionary();
		value.put(StringCodec.UTF_8.encode("NUM_PARTS"), VarIntTool.encode(exportResponse.numParts()));
		value.put(StringCodec.UTF_8.encode("NUM_DATABEANS"), VarIntTool.encode(exportResponse.numDatabeans()));
		blockfileStorageService.makeMetaDictionaryStorage(exportId).write(key, value);
	}

}

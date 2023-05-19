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

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.plugin.dataexport.service.DatabeanExport.DatabeanExportRequest;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.util.tuple.Range;

public class DatabeanExportToDirectory<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanExportToDirectory.class);

	public record DatabeanExportToDirectoryRequest<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>(
		Directory directory,
		String exportId,
		SortedStorageReaderNode<PK,D,F> node,
		Range<PK> range,
		long maxRows,
		int scanBatchSize,
		ExecutorService prefetchExec,
		Threads writeParallelThreads){
	}

	private final DatabeanExportToDirectoryRequest<PK,D,F> request;
	private final DatabeanExport<PK,D,F> backupRegion;
	private final PhaseTimer timer;

	public DatabeanExportToDirectory(DatabeanExportToDirectoryRequest<PK,D,F> request){
		this.request = request;
		this.timer = new PhaseTimer();
		var databeanExportRequest = new DatabeanExportRequest<>(
				request.exportId,
				request.node,
				request.range,
				request.maxRows,
				request.prefetchExec,
				request.scanBatchSize);
		backupRegion = new DatabeanExport<>(databeanExportRequest);
	}

	public long execute(){
		request.directory.writeParallel(
				PathbeanKey.of(request.exportId + "/" + request.node.getName()),
				backupRegion.makeGzipInputStream(),
				request.writeParallelThreads,
				ByteLength.MIN);
		String timerMessage = String.format(
				"exported %s, %s bytes",
				NumberFormatter.addCommas(backupRegion.getNumRecords()),
				NumberFormatter.addCommas(backupRegion.getRawBytes()));
		timer.add(timerMessage);
		logger.warn("exportId={}, timer={}", request.exportId, timer.toString());
		return backupRegion.getNumRecords();
	}

}

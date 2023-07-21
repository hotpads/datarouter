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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import io.datarouter.bytes.kvfile.io.write.KvFileWriter;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.plugin.dataexport.service.exporting.DatabeanExportService.DatabeanExportRequest;
import io.datarouter.plugin.dataexport.service.exporting.DatabeanExportService.DatabeanExportResponse;
import io.datarouter.plugin.dataexport.service.exporting.DatabeanExportTracker.Nested.DatabeanExportTrackerType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.util.BlockfileDirectoryStorage;

public class DatabeanExport<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private static final int DATABEANS_PER_BLOCK = 1_000;
	private static final int DATABEANS_PER_PART = 1_000_000;
	private static final int PREFETCH_DATABEANS = 10_000;
	private static final Duration LOG_PERIOD = Duration.ofSeconds(5);

	private final DatabeanExportRequest<PK,D,F> request;
	private final DatabeanExportTracker tableTracker;

	public DatabeanExport(DatabeanExportRequest<PK,D,F> request){
		this.request = request;
		tableTracker = new DatabeanExportTracker(
				DatabeanExportTrackerType.TABLE,
				request.exportId(),
				request.node().getClientId().getName(),
				request.node().getFieldInfo().getTableName(),
				1,
				Duration.ZERO);
	}

	public DatabeanExportResponse exportTable(){
		var scanConfig = new Config().setRequestBatchSize(request.scanBatchSize());
		var numDatabeansInPart = new AtomicLong();
		request.node().scan(request.pkRange(), scanConfig)
				.limit(request.maxRows())
				.prefetch(request.prefetchExec(), PREFETCH_DATABEANS)
				.each($ -> {
					numDatabeansInPart.incrementAndGet();
					if(numDatabeansInPart.get() > DATABEANS_PER_PART){
						tableTracker.partId().incrementAndGet();
						numDatabeansInPart.set(0);
					}
				})
				.splitBy($ -> tableTracker.partId().get())
				.forEach(scanner -> exportPart(tableTracker.partId().get(), scanner));
		int numParts = tableTracker.partId().get();
		tableTracker.logProgress();
		return new DatabeanExportResponse(
				request.node().getName(),
				numParts,
				tableTracker.databeanCount().value());
	}

	public void exportPart(int partId, Scanner<D> scanner){
		tableTracker.activePartIds().add(partId);
		var blockfileStorage = new BlockfileDirectoryStorage(request.tableDirectory());
		KvFileWriter<D> kvFileWriter = request.kvFileService().makeKvFileWriter(
				blockfileStorage,
				request.node(),
				partId);
		scanner
				.batch(DATABEANS_PER_BLOCK)
				.each(tableTracker.databeanCount()::incrementBySize)
				.each(tableTracker.rateTracker()::incrementBySize)
				.periodic(LOG_PERIOD, $ -> tableTracker.logProgress())
				.apply(kvFileWriter::write);
		tableTracker.activePartIds().remove(partId);
	}

}

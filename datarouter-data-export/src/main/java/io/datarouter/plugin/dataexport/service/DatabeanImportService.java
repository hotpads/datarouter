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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.CountingInputStream;
import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.bytes.kvfile.KvFileCodec;
import io.datarouter.bytes.kvfile.KvFileReader;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanImportPutMultiExecutor;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanImportScanChunksExecutor;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.util.Count;
import io.datarouter.util.Count.Counts;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;

@Singleton
public class DatabeanImportService{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanImportService.class);

	private static final Config CONFIG = new Config()
			.setNumAttempts(20)
			.setTimeout(Duration.ofSeconds(30));
	private static final Duration LOG_PERIOD = Duration.ofSeconds(5);
	private static final int GZIP_BUFFER_BYTES = ByteLength.ofKiB(64).toBytesInt();

	@Inject
	private DatabeanImportScanChunksExecutor scanChunksExec;
	@Inject
	private DatabeanImportPutMultiExecutor putMultiExec;

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long importFromDirectory(
			Directory directory,
			PathbeanKey pathbeanKey,
			String exportId,
			MapStorageNode<PK,D,F> node,
			int putBatchSize){
		logger.warn("importing {}", pathbeanKey);
		InputStream inputStream = directory.scanChunks(
				pathbeanKey,
				Range.everything(),
				new Threads(scanChunksExec, 4),
				ByteLength.ofMiB(4))
				.apply(MultiByteArrayInputStream::new);
		return runImportAndCloseInputStream(exportId, node, putBatchSize, inputStream);
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long importFromMemory(
			String exportId,
			MapStorageNode<PK,D,F> node,
			byte[] bytes){
		var byteArrayInputStream = new ByteArrayInputStream(bytes);
		return runImportAndCloseInputStream(exportId, node, 1_000, byteArrayInputStream);
	}

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Long runImportAndCloseInputStream(
			String exportId,
			MapStorageNode<PK,D,F> node,
			int putBatchSize,
			InputStream inputStream){
		var counts = new Counts();
		var numDatabeans = counts.add("numDatabeans");
		var numRawBytes = counts.add("numRawBytes");
		var numGzipBytes = counts.add("numGzipBytes");
		var lastKey = new AtomicReference<PK>();
		var rateTracker = new RateTracker();
		int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
		var codec = new DatabeanExportCodec<>(node.getFieldInfo());
		var kvFileCodec = new KvFileCodec<>(codec);
		try(var countingGzipInputStream = new CountingInputStream(inputStream, 64, numGzipBytes::incrementBy);
				var gzipInputStream = new GZIPInputStream(countingGzipInputStream, GZIP_BUFFER_BYTES);
				var countingRawInputStream = new CountingInputStream(gzipInputStream, 64, numRawBytes::incrementBy)){
			new KvFileReader(countingRawInputStream).scanBlockEntries()
					.map(kvFileCodec::decode)
					.batch(putBatchSize)
					.parallelUnordered(new Threads(putMultiExec, numThreads))
					.each(batch -> node.putMulti(batch, CONFIG))
					.each(numDatabeans::incrementBySize)
					.each(rateTracker::incrementBySize)
					.each(batch -> lastKey.set(ListTool.getLast(batch).getKey()))
					.periodic(LOG_PERIOD, batch -> logProgress(
							exportId,
							node.getName(),
							numDatabeans,
							numRawBytes,
							numGzipBytes,
							rateTracker,
							lastKey.get().toString()))
					.count();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		logProgress(
				exportId,
				node.getName(),
				numDatabeans,
				numRawBytes,
				numGzipBytes,
				rateTracker,
				Optional.ofNullable(lastKey.get()).map(PrimaryKey::toString).orElse(""));
		return numDatabeans.value();
	}

	private void logProgress(
			String exportId,
			String nodeName,
			Count numDatabeans,
			Count numRawBytes,
			Count numGzipBytes,
			RateTracker rateTracker,
			String lastKey){
		String compressionString = "?";
		if(numRawBytes.value() > 0 && numGzipBytes.value() > 0){
			double compression = (double)numRawBytes.value() / (double)numGzipBytes.value();
			compressionString = NumberFormatter.format(compression, 1);
		}
		logger.warn(
				"imported databeans={}, perSec={}, perSecAvg={}, rawBytes={}, gzipBytes={}, compression={}"
						+ ", exportId={}, node={}, lastKey={}",
				NumberFormatter.addCommas(numDatabeans.value()),
				rateTracker.perSecDisplay(),
				rateTracker.perSecAvgDisplay(),
				ByteLength.ofBytes(numRawBytes.value()).toDisplay(),
				ByteLength.ofBytes(numGzipBytes.value()).toDisplay(),
				compressionString,
				exportId,
				nodeName,
				lastKey);
		rateTracker.markLogged();
	}

}

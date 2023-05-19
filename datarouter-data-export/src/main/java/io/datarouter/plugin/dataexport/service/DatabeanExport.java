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

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.CountingInputStream;
import io.datarouter.bytes.GzipTool;
import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.bytes.kvfile.KvFileBlock;
import io.datarouter.bytes.kvfile.KvFileEntry;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import io.datarouter.util.Count;
import io.datarouter.util.Count.Counts;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;

public class DatabeanExport<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanExport.class);

	private static final ByteLength BLOCK_SIZE = ByteLength.ofMiB(1);
	private static final int PREFETCH_BLOCKS = 2;
	private static final Duration LOG_PERIOD = Duration.ofSeconds(5);

	public record DatabeanExportRequest<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>(
		String exportId,
		SortedStorageReaderNode<PK,D,F> node,
		Range<PK> pkRange,
		long maxRows,
		ExecutorService prefetchExec,
		int scanBatchSize){
	}

	private final DatabeanExportRequest<PK,D,F> request;

	private final Counts counts = new Counts();
	private final Count numDatabeans = counts.add("numDatabeans");
	private final Count numRawBytes = counts.add("numRawBytes");
	private final Count numGzipBytes = counts.add("numGzipBytes");

	private final RateTracker rateTracker = new RateTracker();

	public DatabeanExport(DatabeanExportRequest<PK,D,F> request){
		this.request = request;
	}

	public InputStream makeGzipInputStream(){
		var config = new Config().setRequestBatchSize(request.scanBatchSize());
		Codec<D,KvFileEntry> codec = new DatabeanExportCodec<>(request.node().getFieldInfo());
		var rawInputStream = request.node.scan(request.pkRange, config)
				.limit(request.maxRows)
				.map(codec::encode)
				.batchByMinSize(BLOCK_SIZE.toBytes(), KvFileEntry::length)
				.prefetch(request.prefetchExec, PREFETCH_BLOCKS)
				.each(numDatabeans::incrementBySize)
				.each(rateTracker::incrementBySize)
				.periodic(LOG_PERIOD, $ -> logProgress())
				.map(KvFileBlock::new)
				.map(KvFileBlock::toBytes)
				.apply(MultiByteArrayInputStream::new);
		var countingRawInputStream = new CountingInputStream(
				rawInputStream,
				ByteLength.ofKiB(64).toBytesInt(),
				numRawBytes::incrementBy);
		return new CountingInputStream(
				GzipTool.encodeToInputStream(countingRawInputStream),
				ByteLength.ofKiB(64).toBytesInt(),
				numGzipBytes::incrementBy);
	}

	private void logProgress(){
		String compressionString = "?";
		if(numRawBytes.value() > 0 && numGzipBytes.value() > 0){
			double compression = (double)numRawBytes.value() / (double)numGzipBytes.value();
			compressionString = NumberFormatter.format(compression, 1);
		}
		logger.warn(
				"exported databeans={}, perSec={}, perSecAvg={}, rawBytes={}, gzipBytes={}, compression={}"
						+ ", exportId={}, node={}",
				NumberFormatter.addCommas(numDatabeans.value()),
				rateTracker.perSecDisplay(),
				rateTracker.perSecAvgDisplay(),
				ByteLength.ofBytes(numRawBytes.value()).toDisplay(),
				ByteLength.ofBytes(numGzipBytes.value()).toDisplay(),
				compressionString,
				request.exportId,
				request.node.getName());
		rateTracker.markLogged();
	}

	public long getNumRecords(){
		return numDatabeans.value();
	}

	public long getRawBytes(){
		return numRawBytes.value();
	}

}

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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

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
import io.datarouter.util.tuple.Range;

public class DatabeanExport<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanExport.class);

	public static final Config DATABEAN_CONFIG = new Config()
			.setNumAttempts(30)
			.setTimeout(Duration.ofSeconds(10));
	private static final ByteLength BLOCK_SIZE = ByteLength.ofMiB(1);
	private static final Duration LOG_PERIOD = Duration.ofSeconds(5);

	private final String exportId;
	private final SortedStorageReaderNode<PK,D,F> node;
	private final Config config;
	private final Range<PK> pkRange;
	private final Predicate<D> predicate;
	private final long maxRows;
	private final ExecutorService prefetchExec;

	private final Counts counts = new Counts();
	private final Count numDatabeans = counts.add("numDatabeans");
	private final Count numRawBytes = counts.add("numRawBytes");
	private final Count numGzipBytes = counts.add("numGzipBytes");

	public DatabeanExport(
			String exportId,
			SortedStorageReaderNode<PK,D,F> node,
			Config config,
			Range<PK> pkRange,
			Predicate<D> predicate,
			long maxRows,
			ExecutorService prefetchExec){
		this.exportId = exportId;
		this.node = node;
		this.config = config;
		this.pkRange = pkRange;
		this.predicate = Optional.ofNullable(predicate).orElse($ -> true);
		this.maxRows = maxRows;
		this.prefetchExec = prefetchExec;
	}

	public InputStream makeGzipInputStream(){
		Codec<D,KvFileEntry> codec = new DatabeanExportCodec<>(node.getFieldInfo());
		var rawInputStream = node.scan(pkRange, config)
				.prefetch(prefetchExec, 10)
				.advanceWhile(predicate)
				.advanceWhile($ -> numDatabeans.value() < maxRows)
				.map(codec::encode)
				.batchByMinSize(BLOCK_SIZE.toBytes(), KvFileEntry::length)
				.each(numDatabeans::incrementBySize)
				.periodic(LOG_PERIOD, $ -> logger.warn(
						"exported {}, exportId={}, node={}",
						counts,
						exportId,
						node.getName()))
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

	public long getNumRecords(){
		return numDatabeans.value();
	}

	public long getRawBytes(){
		return numRawBytes.value();
	}

}

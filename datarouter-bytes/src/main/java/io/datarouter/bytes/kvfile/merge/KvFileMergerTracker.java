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
package io.datarouter.bytes.kvfile.merge;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.dto.BlockfileNameAndSize;
import io.datarouter.scanner.Scanner;

public class KvFileMergerTracker{
	private static final Logger logger = LoggerFactory.getLogger(KvFileMergerTracker.class);

	public final KvFileMergePlan plan;
	public final String filename;
	public final AtomicLong lastLogTimeNs;

	public Instant startTime;
	public Instant mergeStartTime;

	public final AtomicLong compressedBytesRead = new AtomicLong();
	public final AtomicLong compressedBytesReadSinceLastLog = new AtomicLong();
	public final AtomicLong decompressedBytesRead = new AtomicLong();
	public final AtomicLong decompressedBytesReadSinceLastLog = new AtomicLong();
	public final AtomicLong blocksRead = new AtomicLong();
	public final AtomicLong blocksReadSinceLastLog = new AtomicLong();
	public final AtomicLong recordsRead = new AtomicLong();
	public final AtomicLong recordsReadSinceLastLog = new AtomicLong();
	public final AtomicLong recordsWritten = new AtomicLong();
	public final AtomicLong recordsWrittenSinceLastLog = new AtomicLong();
	public final AtomicLong blocksWritten = new AtomicLong();
	public final AtomicLong blocksWrittenSinceLastLog = new AtomicLong();

	public final AtomicLong waitForReadersNs = new AtomicLong();
	public final AtomicLong waitForBlocksNs = new AtomicLong();
	public final AtomicLong waitForCollatorNs = new AtomicLong();

	public KvFileMergerTracker(KvFileMergePlan plan, String filename){
		this.plan = plan;
		this.filename = filename;
		lastLogTimeNs = new AtomicLong();
	}

	public void resetCountersSinceLastLog(){
		lastLogTimeNs.set(System.nanoTime());
		compressedBytesReadSinceLastLog.set(0);
		decompressedBytesReadSinceLastLog.set(0);
		blocksReadSinceLastLog.set(0);
		recordsReadSinceLastLog.set(0);
		recordsWrittenSinceLastLog.set(0);
		blocksWrittenSinceLastLog.set(0);
	}

	public void logInitializationStats(){
		Function<Number,String> withCommas = number -> new DecimalFormat("###,###,###,###,###,###,###").format(number);
		logger.warn(
				"initialized compressedBytes={}, decompressedBytes={}, blocks={}, records={}",
				ByteLength.ofBytes(compressedBytesRead.get()).toDisplay(),
				ByteLength.ofBytes(decompressedBytesRead.get()).toDisplay(),
				withCommas.apply(blocksRead.get()),
				withCommas.apply(recordsRead.get()));
	}

	public void logIntermediateProgress(){
		logProgress(false, null);
	}

	public void logProgress(
			boolean complete,
			BlockfileNameAndSize newFile){
		Duration totalDuration = Duration.between(startTime, Instant.now());
		Duration mergeDuration = Duration.between(mergeStartTime, Instant.now());
		long nsSinceLastLog = System.nanoTime() - lastLogTimeNs.get();
		String action = complete ? "merged" : "merging";
		Function<Number,String> withCommas = number -> new DecimalFormat("###,###,###,###,###,###,###").format(number);
		Function<Long,String> nanosToString = nanos -> withCommas.apply(nanos / 1_000_000) + "ms";
		var bigBillion = BigInteger.valueOf(1_000_000_000);
		var bigMergeDurationNs = BigInteger.valueOf(mergeDuration.toNanos());
		var bigNsSinceLastLog = BigInteger.valueOf(nsSinceLastLog);
		Function<Long,Long> perSecondLatestFn = count -> BigInteger.valueOf(count)
				.multiply(bigBillion)
				.divide(bigNsSinceLastLog)
				.longValue();
		Function<Long,Long> perSecondAvgFn = count -> BigInteger.valueOf(count)
				.multiply(bigBillion)
				.divide(bigMergeDurationNs)
				.longValue();

		// read pct completion
		double readCompletionRatio = (double)compressedBytesRead.get()
				/ plan.totalInputSize().toBytes();
		if(complete){
			readCompletionRatio = 1;// Otherwise it will never get to 1 because of blockfile metadata.
		}
		String readCompletionPctString = new DecimalFormat("#.##").format(100 * readCompletionRatio) + "%";

		// latest rates
		long compressedBytesRpsLatest = perSecondLatestFn.apply(compressedBytesReadSinceLastLog.get());
		long decompressedBytesRpsLatest = perSecondLatestFn.apply(decompressedBytesReadSinceLastLog.get());
		long blocksRpsLatest = perSecondLatestFn.apply(blocksReadSinceLastLog.get());
		long recordsRpsLatest = perSecondLatestFn.apply(recordsReadSinceLastLog.get());
		long recordsWpsLatest = perSecondLatestFn.apply(recordsWrittenSinceLastLog.get());
		long blocksWpssLatest = perSecondLatestFn.apply(blocksWrittenSinceLastLog.get());

		// avg rates
		long compressedBytesRpsAvg = perSecondAvgFn.apply(compressedBytesRead.get());
		long decompressedBytesRpsAvg = perSecondAvgFn.apply(decompressedBytesRead.get());
		long blocksRpsAvg = perSecondAvgFn.apply(blocksRead.get());
		long recordsRpsAvg = perSecondAvgFn.apply(recordsRead.get());
		long recordsWpsAvg = perSecondAvgFn.apply(recordsWritten.get());
		long blocksWpsAvg = perSecondAvgFn.apply(blocksWritten.get());

		// times
		// TODO i'm not sure how meaningful these are in the current state
		long waitForReadersNsActual = waitForReadersNs.get();
		long waitForBlocksNsActual = waitForBlocksNs.get();
		long waitForCollatorNsActual = waitForCollatorNs.get() - waitForBlocksNsActual;

		Map<String,String> planAttrs = new LinkedHashMap<>();
		planAttrs.put("progress", readCompletionPctString);
		planAttrs.put(
				"files",
				withCommas.apply(plan.files().size())
						+ "/" + withCommas.apply(plan.numCompactorFiles()));
		planAttrs.put("levels", plan.levels().stream().map(Number::toString).collect(Collectors.joining("/")));
		planAttrs.put("bytes", plan.totalInputSize().toDisplay()
				+ "/" + plan.numCompactorBytes().toDisplay());
		planAttrs.put("collator", plan.collatorStrategy().name());
		planAttrs.put("filename", filename);

		Map<String,String> compressedByteReadAttrs = new LinkedHashMap<>();
		compressedByteReadAttrs.put(
				"compressedBytesRead",
				ByteLength.ofBytes(compressedBytesRead.get()).toDisplay());
		compressedByteReadAttrs.put("perSec", ByteLength.ofBytes(compressedBytesRpsLatest).toDisplay());
		compressedByteReadAttrs.put("perSecAvg", ByteLength.ofBytes(compressedBytesRpsAvg).toDisplay());

		Map<String,String> decompressedByteReadAttrs = new LinkedHashMap<>();
		decompressedByteReadAttrs.put(
				"decompressedBytesRead",
				ByteLength.ofBytes(decompressedBytesRead.get()).toDisplay());
		decompressedByteReadAttrs.put("perSec", ByteLength.ofBytes(decompressedBytesRpsLatest).toDisplay());
		decompressedByteReadAttrs.put("perSecAvg", ByteLength.ofBytes(decompressedBytesRpsAvg).toDisplay());

		Map<String,String> recordReadAttrs = new LinkedHashMap<>();
		recordReadAttrs.put("count", withCommas.apply(recordsRead.get()));
		recordReadAttrs.put("perSec", withCommas.apply(recordsRpsLatest));
		recordReadAttrs.put("perSecAvg", withCommas.apply(recordsRpsAvg));

		Map<String,String> blockReadAttrs = new LinkedHashMap<>();
		blockReadAttrs.put("count", withCommas.apply(blocksRead.get()));
		blockReadAttrs.put("perSec", withCommas.apply(blocksRpsLatest));
		blockReadAttrs.put("perSecAvg", withCommas.apply(blocksRpsAvg));

		Map<String,String> recordWriteAttrs = new LinkedHashMap<>();
		recordWriteAttrs.put("count", withCommas.apply(recordsWritten.get()));
		recordWriteAttrs.put("perSec", withCommas.apply(recordsWpsLatest));
		recordWriteAttrs.put("perSecAvg", withCommas.apply(recordsWpsAvg));

		Map<String,String> blockWriteAttrs = new LinkedHashMap<>();
		blockWriteAttrs.put("count", withCommas.apply(blocksWritten.get()));
		blockWriteAttrs.put("perSec", withCommas.apply(blocksWpssLatest));
		blockWriteAttrs.put("perSecAvg", withCommas.apply(blocksWpsAvg));

		Map<String,String> otherAttrs = new LinkedHashMap<>();
		otherAttrs.put("totalDuration", nanosToString.apply(totalDuration.toNanos()).toString());
		otherAttrs.put("mergeDuration", nanosToString.apply(mergeDuration.toNanos()).toString());
		otherAttrs.put("waitForReaders", nanosToString.apply(waitForReadersNsActual));
		otherAttrs.put("waitForBlocks", nanosToString.apply(waitForBlocksNsActual));
		otherAttrs.put("waitForCollator", nanosToString.apply(waitForCollatorNsActual));

		Function<Map<String,String>,String> toLineFn = map -> Scanner.of(map.keySet())
				.map(key -> key + "=" + map.get(key))
				.collect(Collectors.joining(", ", "[", "]"));
		String linePrefix = "\n  ";
		List<String> lines = List.of(
				"kvMergePlan             " + toLineFn.apply(planAttrs),
				"kvReadCompressedBytes   " + toLineFn.apply(compressedByteReadAttrs),
				"kvReadDecompressedBytes " + toLineFn.apply(decompressedByteReadAttrs),
				"kvReadBlocks            " + toLineFn.apply(blockReadAttrs),
				"kvReadRecords           " + toLineFn.apply(recordReadAttrs),
				"kvWriteRecords          " + toLineFn.apply(recordWriteAttrs),
				"kvWriteBlocks           " + toLineFn.apply(blockWriteAttrs),
				"kvTime                  " + toLineFn.apply(otherAttrs));
		String message = String.format(
				"%s, newFile=%s %s",
				action,
				Optional.ofNullable(newFile).map(BlockfileNameAndSize::toString).orElse("?"),
				lines.stream().collect(Collectors.joining(linePrefix, linePrefix, "")));
		logger.warn(message);
	}

}

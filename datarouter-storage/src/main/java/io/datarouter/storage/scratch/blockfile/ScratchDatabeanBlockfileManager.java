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
package io.datarouter.storage.scratch.blockfile;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.io.storage.BlockfileNameAndSize;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.scratch.ScratchDatabeanCodec;
import io.datarouter.storage.scratch.ScratchDatabeanCodec.ScratchDatabeanBytes;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.util.BlockfileDirectoryStorage;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Range;

public class ScratchDatabeanBlockfileManager<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final String name;
	private final BlockfileGroup<D> blockfileGroup;
	private final BlockfileCompressor compressor;
	private final Codec<D,BlockfileRow> codec;
	private final Function<Range<PK>,Scanner<D>> inputScannerSupplier;
	private final Threads writeThreads;
	private final Threads readThreads;
	private final Threads decodeThreads;
	private final Optional<TaskTracker> optTracker;

	private ScratchDatabeanBlockfileManager(
			String name,
			BlockfileGroup<D> blockfileGroup,
			BlockfileCompressor compressor,
			Codec<D,BlockfileRow> codec,
			Function<Range<PK>,Scanner<D>> inputScannerSupplier,
			Threads writeThreads,
			Threads readThreads,
			Threads decodeThreads,
			Optional<TaskTracker> optTracker){
		this.name = name;
		this.blockfileGroup = blockfileGroup;
		this.compressor = compressor;
		this.codec = codec;
		this.inputScannerSupplier = inputScannerSupplier;
		this.writeThreads = writeThreads;
		this.readThreads = readThreads;
		this.decodeThreads = decodeThreads;
		this.optTracker = optTracker;
	}

	public void writeBlockfiles(List<Range<PK>> ranges){
		var completedRangeCounter = new AtomicLong();
		Scanner.iterate(0, rangeIndex -> rangeIndex + 1)
				.limit(ranges.size())
				.parallelUnordered(writeThreads)
				.forEach(rangeIndex -> writeBlockfile(
						rangeIndex,
						ranges.size(),
						ranges.get(rangeIndex),
						completedRangeCounter));
	}

	private void writeBlockfile(
			long rangeIndex,
			long numRanges,
			Range<PK> range,
			AtomicLong completedRangeCounter){
		String filename = makeFilename(rangeIndex + 1, numRanges);// one-based filenames
		BlockfileWriter<D> writer = blockfileGroup.newWriterBuilder(filename)
				.setCompressor(compressor)
				.build();
		var itemCountThisPeriod = new AtomicLong();
		Scanner<BlockfileRow> rows = inputScannerSupplier.apply(range)
				.map(codec::encode)
				.batch(1_000)// batch for monitoring
				.each(batch -> itemCountThisPeriod.addAndGet(batch.size()))
				.periodic(// protect against slow TaskTracker implementation
						Duration.ofSeconds(1),
						batch -> {
							optTracker.ifPresent(tracker -> {
								tracker.increment(itemCountThisPeriod.get()).heartbeat();
								itemCountThisPeriod.set(0);
							});
				})
				.concat(Scanner::of);
		writer.writeRows(ByteLength.ofKiB(16), rows);
		completedRangeCounter.incrementAndGet();
		optTracker.ifPresent(tracker -> {
			String lastItemProcessedMessage = String.format(
					"%s blockfile %s of %s",
					name,
					NumberFormatter.addCommas(completedRangeCounter.get()),
					NumberFormatter.addCommas(numRanges));
			tracker.setLastItemProcessed(lastItemProcessedMessage);
		});
	}

	public Scanner<D> scanBlockfiles(){
		return Scanner.of(blockfileGroup.storage().list())
				.map(BlockfileNameAndSize::name)
				.concat(this::scanBlockfile);
	}

	private Scanner<D> scanBlockfile(String filename){
		BlockfileReader<D> reader = blockfileGroup.newReaderBuilder(filename, codec::decode)
				.setReadThreads(readThreads)
				.setDecodeThreads(decodeThreads)
				.setReadChunkSize(ByteLength.ofMiB(16))
				.build();
		return reader.sequential().scan();
	}

	private static String makeFilename(long rangeId, long numRanges){
		int length = Long.toString(numRanges).length();
		String paddedRangeId = StringTool.pad(Long.toString(rangeId), '0', length);
		return paddedRangeId + "-of-" + numRanges;
	}

	/*------- builder ---------*/

	public static class ScratchDatabeanBlockfileManagerBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>{

		private final String name;
		private final Directory directory;
		private final Codec<D,ScratchDatabeanBytes> databeanBytesCodec;
		private final Function<Range<PK>,Scanner<D>> inputScannerSupplier;
		private BlockfileCompressor compressor = BlockfileStandardCompressors.NONE;
		private Threads writeThreads = Threads.none();
		private Threads readThreads = Threads.none();
		private Threads decodeThreads = Threads.none();
		private TaskTracker tracker;

		public ScratchDatabeanBlockfileManagerBuilder(
				String name,
				Directory directory,
				Codec<D,ScratchDatabeanBytes> databeanBytesCodec,
				Function<Range<PK>,Scanner<D>> inputScannerSupplier){
			this.name = name;
			this.directory = directory;
			this.databeanBytesCodec = databeanBytesCodec;
			this.inputScannerSupplier = inputScannerSupplier;
		}

		public static <
				PK extends PrimaryKey<PK>,
				D extends Databean<PK,D>,
				F extends DatabeanFielder<PK,D>>
		ScratchDatabeanBlockfileManagerBuilder<PK,D,F> fromDatabeanFieldInfo(
				String name,
				Directory directory,
				DatabeanFieldInfo<PK,D,F> fieldInfo,
				Function<Range<PK>,Scanner<D>> inputScannerSupplier){
			return new ScratchDatabeanBlockfileManagerBuilder<>(
					name,
					directory,
					new ScratchDatabeanCodec<>(fieldInfo),
					inputScannerSupplier);
		}

		public static <
				PK extends PrimaryKey<PK>,
				D extends Databean<PK,D>,
				F extends DatabeanFielder<PK,D>>
		ScratchDatabeanBlockfileManagerBuilder<PK,D,F> fromIndexEntryFieldInfo(
				String name,
				Directory directory,
				IndexEntryFieldInfo<PK,D,F> fieldInfo,
				Function<Range<PK>,Scanner<D>> inputScannerSupplier){
			return new ScratchDatabeanBlockfileManagerBuilder<>(
					name,
					directory,
					new ScratchDatabeanCodec<>(fieldInfo),
					inputScannerSupplier);
		}

		public ScratchDatabeanBlockfileManagerBuilder<PK,D,F> setCompressor(BlockfileCompressor compressor){
			this.compressor = compressor;
			return this;
		}

		public ScratchDatabeanBlockfileManagerBuilder<PK,D,F> setWriteThreads(Threads threads){
			this.writeThreads = threads;
			return this;
		}

		public ScratchDatabeanBlockfileManagerBuilder<PK,D,F> setReadThreads(Threads threads){
			this.readThreads = threads;
			return this;
		}

		public ScratchDatabeanBlockfileManagerBuilder<PK,D,F> setDecodeThreads(Threads threads){
			this.decodeThreads = threads;
			return this;
		}

		public ScratchDatabeanBlockfileManagerBuilder<PK,D,F> setTaskTracker(TaskTracker tracker){
			this.tracker = tracker;
			return this;
		}

		public ScratchDatabeanBlockfileManager<PK,D,F> build(){
			var blockfileStorage = new BlockfileDirectoryStorage(directory);
			var blockfileGroup = new BlockfileGroupBuilder<D>(blockfileStorage).build();
			var codec = new ScratchDatabeanBlockfileCodec<>(databeanBytesCodec);
			return new ScratchDatabeanBlockfileManager<>(
					name,
					blockfileGroup,
					compressor,
					codec,
					inputScannerSupplier,
					writeThreads,
					readThreads,
					decodeThreads,
					Optional.ofNullable(tracker));
		}
	}

}

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
package io.datarouter.filesystem.snapshot;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.bytestringcodec.CsvIntByteStringCodec;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlockV1;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlockV1Encoder;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlockV1;
import io.datarouter.filesystem.snapshot.block.value.ValueBlockV1Encoder;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;
import io.datarouter.scanner.Ref;
import io.datarouter.scanner.RetainingGroup;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;
import io.datarouter.util.lang.ObjectTool;

public class WordTests{
	private static final Logger logger = LoggerFactory.getLogger(WordTests.class);

	@Test
	public void testValueBlockV1(){
		Supplier<ValueBlockV1Encoder> encoderSupplier = ValueBlockV1Encoder::new;
		Ref<ValueBlockV1Encoder> encoder = new Ref<>(encoderSupplier.get());
		int blockSize = 4096;
		List<String> inputs = WordDataset.scanWords(getClass().getSimpleName() + "-testValueBlockV1").list();
		List<byte[]> blocks = Scanner.of(inputs)
				.map(str -> str.getBytes(StandardCharsets.UTF_8))
				.map(value -> new SnapshotEntry(EmptyArray.BYTE, EmptyArray.BYTE, new byte[][]{value}))
				.concat(value -> {
					encoder.get().add(value, 0);
					if(encoder.get().numBytes() >= blockSize){
						byte[] block = encoder.get().encode().concat();
						encoder.set(encoderSupplier.get());
						return Scanner.of(block);
					}
					return Scanner.empty();
				})
				.list();
		if(encoder.get().numRecords() > 0){
			blocks.add(encoder.get().encode().concat());
		}
		logger.warn("encoded {} value blocks", blocks.size());
		List<String> outputs = Scanner.of(blocks)
				.map(ValueBlockV1::new)
				.concat(ValueBlock::valueCopies)
				.map(bytes -> new String(bytes, StandardCharsets.UTF_8))
				.list();

		Require.equals(outputs.size(), inputs.size());
		for(int i = 0; i < inputs.size(); ++i){
			if(ObjectTool.notEquals(outputs.get(i), inputs.get(i))){
				logger.warn("actual=[{}] expected=[{}]", outputs.get(i), inputs.get(i));
				String message = String.format("actual=[%s] does not equal expected=[%s]",
						CsvIntByteStringCodec.INSTANCE.encode(outputs.get(i).getBytes(StandardCharsets.UTF_8)),
						CsvIntByteStringCodec.INSTANCE.encode(inputs.get(i).getBytes(StandardCharsets.UTF_8)));
				throw new IllegalArgumentException(message);
			}
		}
	}

	@Test
	public void testLeafBlockV1(){
		Supplier<LeafBlockV1Encoder> encoderSupplier = () -> new LeafBlockV1Encoder(32 * 1024);
		Ref<LeafBlockV1Encoder> encoder = new Ref<>(encoderSupplier.get());
		int blockSize = 4096;
		String valuePrefix = "val_";
		List<SnapshotEntry> inputs = WordDataset.scanWords(getClass().getSimpleName() + "-testLeafBlockV1")
				.map(word -> {
					byte[] keyBytes = word.getBytes(StandardCharsets.UTF_8);
					byte[] valueBytes = ByteTool.concatenate2(valuePrefix.getBytes(StandardCharsets.UTF_8), keyBytes);
					return new SnapshotEntry(keyBytes, valueBytes, ByteTool.EMPTY_ARRAY_2);
				})
				.list();
		var keyId = new AtomicLong();
		List<byte[]> blocks = Scanner.of(inputs)
				.concat(entry -> {
					//TODO use real value block references
					encoder.get().add(0, keyId.getAndIncrement(), entry, new int[]{0}, new int[]{0});
					if(encoder.get().numBytes() >= blockSize){
						var fileIdsAndEndings = new FileIdsAndEndings[]{
								new FileIdsAndEndings(new int[]{0}, new int[]{0})
						};
						byte[] block = encoder.get().encode(fileIdsAndEndings).concat();
						encoder.set(encoderSupplier.get());
						return Scanner.of(block);
					}
					return Scanner.empty();
				})
				.list();
		if(encoder.get().numRecords() > 0){
			var fileIdsAndEndings = new FileIdsAndEndings[]{
					new FileIdsAndEndings(new int[]{0}, new int[]{0})
			};
			blocks.add(encoder.get().encode(fileIdsAndEndings).concat());
		}
		logger.warn("encoded {} key blocks", blocks.size());
		List<SnapshotLeafRecord> outputs = Scanner.of(blocks)
				.map(LeafBlockV1::new)
				.concat(LeafBlock::leafRecords)
				.list();

		Require.equals(outputs.size(), inputs.size());
		for(int i = 0; i < outputs.size(); ++i){
			if(!Arrays.equals(outputs.get(i).key, inputs.get(i).key())){
				logger.warn("actual=[{}] expected=[{}]", outputs.get(i), inputs.get(i));
				String message = String.format("key: actual=[%s] does not equal expected=[%s]",
						CsvIntByteStringCodec.INSTANCE.encode(outputs.get(i).key),
						CsvIntByteStringCodec.INSTANCE.encode(inputs.get(i).key()));
				throw new IllegalArgumentException(message);
			}
			if(!Arrays.equals(outputs.get(i).value, inputs.get(i).value())){
				logger.warn("actual=[{}] expected=[{}]", outputs.get(i), inputs.get(i));
				String message = String.format("value: actual=[%s] does not equal expected=[%s]",
						CsvIntByteStringCodec.INSTANCE.encode(outputs.get(i).value),
						CsvIntByteStringCodec.INSTANCE.encode(inputs.get(i).value()));
				throw new IllegalArgumentException(message);
			}
		}
	}

	@Test
	public void testWordDataLength(){
		long numChars = WordDataset.scanWords(getClass().getSimpleName() + "-testWordDataLength")
				.retain(1)
				.each(retained -> {
					if(retained.previous() != null){
						Require.greaterThan(retained.current(), retained.previous());
					}
				})
				.map(RetainingGroup::current)
				.map(String::length)
				.map(Integer::longValue)
				.reduce(0L, Long::sum);
		Assert.assertTrue(numChars > 1_000_000);
	}

}

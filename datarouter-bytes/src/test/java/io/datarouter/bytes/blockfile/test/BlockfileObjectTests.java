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
package io.datarouter.bytes.blockfile.test;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.storage.impl.BlockfileLocalStorage;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.blockfile.row.BlockfileRowOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;

public class BlockfileObjectTests{

	private static final StringCodec STRING_CODEC = StringCodec.UTF_8;
	private static final BlockfileStorage STORAGE = new BlockfileLocalStorage("/tmp/datarouterTest/blockfile/object/");
	private static final String BLOCK_FILENAME = "block";
	private static final int BLOCK_SIZE = 10;
	private static final int NUM_VALUE_BLOCKS = 100;
	private static final int INDEX_FAN_OUT = 8;

	private record TestDto(
			String key,
			String version,
			BlockfileRowOp op,
			String value){

		static final Codec<TestDto,BlockfileRow> BLOCKFILE_ROW_CODEC = Codec.of(
				dto -> BlockfileRow.create(
						STRING_CODEC.encode(dto.key),
						STRING_CODEC.encode(dto.version),
						dto.op,
						STRING_CODEC.encode(dto.value)),
				row -> new TestDto(
						STRING_CODEC.decode(row.copyOfKey()),
						STRING_CODEC.decode(row.copyOfVersion()),
						row.op(),
						STRING_CODEC.decode(row.copyOfValue())));
	}

	private static final List<TestDto> DTOS = Scanner.iterate(0, i -> i + 1)
			.limit(NUM_VALUE_BLOCKS * BLOCK_SIZE)
			.map(i -> new TestDto(
					// padding not technically needed, but it's good for keys to be sortable
					"key-" + intToPaddedString(i),
					"version-" + i,
					BlockfileRowOp.PUT,
					"value-" + i))
			.list();

	@Test
	private void testViaBlockfile(){
		var blockfileGroup = new BlockfileGroupBuilder<TestDto>(STORAGE).build();
		var writer = blockfileGroup.newWriterBuilder(BLOCK_FILENAME)
				.setHeaderDictionary(new BinaryDictionary().put("hk", "hv"))
				.setFooterDictionarySupplier(() -> new BinaryDictionary().put("fk", "fv"))
				.setIndexFanOut(INDEX_FAN_OUT)
				.build();
		Scanner.of(DTOS)
				.map(TestDto.BLOCKFILE_ROW_CODEC::encode)
				.batch(BLOCK_SIZE)
				.apply(writer::writeBlocks);
		var reader = blockfileGroup.newReaderBuilder(BLOCK_FILENAME, TestDto.BLOCKFILE_ROW_CODEC::decode)
				.build();
		List<TestDto> decoded = reader.sequential().scan().list();
		Assert.assertEquals(decoded, DTOS);
		Assert.assertEquals(reader.metadata().footer().numValueBlocks(), NUM_VALUE_BLOCKS);
		Assert.assertEquals(reader.metadata().header().userDictionary().findStringValue("hk").orElseThrow(), "hv");
		Assert.assertEquals(reader.metadata().footer().userDictionary().findStringValue("fk").orElseThrow(), "fv");
	}

	private static String intToPaddedString(int value){
		int desiredLength = 10;
		String unpadded = Integer.toString(value);
		int paddingLength = desiredLength - unpadded.length();
		return "0".repeat(paddingLength) + unpadded;
	}

}

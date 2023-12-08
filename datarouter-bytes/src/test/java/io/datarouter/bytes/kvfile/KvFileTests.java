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
package io.datarouter.bytes.kvfile;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.BlockfileBuilder;
import io.datarouter.bytes.blockfile.storage.BlockfileLocalStorage;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.kvfile.block.KvFileBlockCodec;
import io.datarouter.bytes.kvfile.blockformat.KvFileStandardBlockFormats;
import io.datarouter.bytes.kvfile.io.KvFileBuilder;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.bytes.kvfile.kv.KvFileOp;
import io.datarouter.scanner.Scanner;

public class KvFileTests{

	private static final StringCodec STRING_CODEC = StringCodec.UTF_8;
	private static final BlockfileStorage STORAGE = new BlockfileLocalStorage("/tmp/datarouterTest/kvfile/");
	private static final String BLOCK_RAW_FILENAME = "blockRaw";
	private static final String BLOCK_FILENAME = "block";
	private static final String KV_FILENAME = "kv";
	private static final int BLOCK_SIZE = 10;
	private static final int NUM_BLOCKS = 100;

	private record TestDto(
			String key,
			String version,
			KvFileOp op,
			String value){

		static final Codec<TestDto,KvFileEntry> KV_CODEC = Codec.of(
				dto -> KvFileEntry.create(
						STRING_CODEC.encode(dto.key),
						STRING_CODEC.encode(dto.version),
						dto.op,
						STRING_CODEC.encode(dto.value)),
				kv -> new TestDto(
						STRING_CODEC.decode(kv.copyOfKey()),
						STRING_CODEC.decode(kv.copyOfVersion()),
						kv.op(),
						STRING_CODEC.decode(kv.copyOfValue())));
	}

	private static final List<TestDto> DTOS = Scanner.iterate(0, i -> i + 1)
			.limit(NUM_BLOCKS * BLOCK_SIZE)
			.map(i -> new TestDto(
					// padding not technically needed, but it's good for keys to be sortable
					"key-" + intToPaddedString(i),
					"version-" + i,
					KvFileOp.PUT,
					"value-" + i))
			.list();

	// The test does the encoding to KvFileEntry.  Not sure it's needed.
	@Test
	private void testViaBlockfileRaw(){
		KvFileBlockCodec<KvFileEntry> blockCodec = KvFileStandardBlockFormats.SEQUENTIAL.newBlockCodec();
		var blockfile = new BlockfileBuilder<List<KvFileEntry>>(STORAGE).build();
		var writer = blockfile.newWriterBuilder(
				BLOCK_RAW_FILENAME,
				blockCodec::encodeAll)
				.build();
		Scanner.of(DTOS)
				.map(TestDto.KV_CODEC::encode)
				.batch(BLOCK_SIZE)
				.apply(writer::write);
		var metadataReader = blockfile.newMetadataReaderBuilder(BLOCK_RAW_FILENAME).build();
		var reader = blockfile.newReaderBuilder(
				metadataReader,
				$ -> blockCodec::decodeAll)
				.build();
		List<TestDto> decoded = reader.scanDecodedValues()
				.concat(Scanner::of)
				.map(TestDto.KV_CODEC::decode)
				.list();
		Assert.assertEquals(decoded, DTOS);
	}

	// Configures the KvFile via the underlying Blockfile
	@Test
	private void testViaBlockfile(){
		KvFileBlockCodec<TestDto> blockCodec = KvFileStandardBlockFormats.SEQUENTIAL.newBlockCodec(TestDto.KV_CODEC);
		var blockfile = new BlockfileBuilder<List<TestDto>>(STORAGE).build();
		var writer = blockfile.newWriterBuilder(BLOCK_FILENAME, blockCodec::encodeAll).build();
		Scanner.of(DTOS)
				.batch(BLOCK_SIZE)
				.apply(writer::write);
		var metadataReader = blockfile.newMetadataReaderBuilder(BLOCK_FILENAME).build();
		var reader = blockfile.newReaderBuilder(metadataReader, $ -> blockCodec::decodeAll).build();
		List<TestDto> decoded = reader.scanDecodedValues()
				.concat(Scanner::of)
				.list();
		Assert.assertEquals(decoded, DTOS);
		Assert.assertEquals(reader.footer().blockCount(), NUM_BLOCKS);
	}

	// Uses the KvFile layer for configuration.  This is what users will do.
	@Test
	private void testViaKvFile(){
		var kvFile = new KvFileBuilder<TestDto>(STORAGE).build();
		var writer = kvFile.newWriterBuilder(
				KV_FILENAME,
				TestDto.KV_CODEC,
				KvFileStandardBlockFormats.SEQUENTIAL)
				.setHeaderDictionary(new BinaryDictionary().put("hk", "hv"))
				.setFooterDictionarySupplier(() -> new BinaryDictionary().put("fk", "fv"))
				.build();
		Scanner.of(DTOS)
				.batch(BLOCK_SIZE)
				.apply(writer::write);
		var reader = kvFile.newReaderBuilder(KV_FILENAME, TestDto.KV_CODEC).build();
		List<TestDto> decoded = reader.scan()
				.list();
		Assert.assertEquals(decoded, DTOS);
		Assert.assertEquals(reader.header().blockFormat(), KvFileStandardBlockFormats.SEQUENTIAL);
		Assert.assertEquals(reader.blockfileFooter().blockCount(), NUM_BLOCKS);
		Assert.assertEquals(reader.footer().kvCount(), DTOS.size());
		Assert.assertEquals(reader.header().userDictionary().findStringValue("hk").orElseThrow(), "hv");
		Assert.assertEquals(reader.footer().userDictionary().findStringValue("fk").orElseThrow(), "fv");
	}

	private static String intToPaddedString(int value){
		int desiredLength = 10;
		String unpadded = Integer.toString(value);
		int paddingLength = desiredLength - unpadded.length();
		return "0".repeat(paddingLength) + unpadded;
	}

}

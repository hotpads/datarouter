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
package io.datarouter.binarydto;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.multi.bytearray.BinaryDtoByteArrayScanner;
import io.datarouter.binarydto.multi.bytearray.MultiBinaryDtoEncoder;
import io.datarouter.bytes.BatchingByteArrayScanner;
import io.datarouter.bytes.ByteTool;
import io.datarouter.scanner.Scanner;

public class BatchingByteArrayScannerIntegrationTests{

	private static class LongDto extends BinaryDto<LongDto>{

		@SuppressWarnings("unused")
		public final long value;

		public static LongDto decode(byte[] bytes){
			return BinaryDtoIndexedCodec.of(LongDto.class).decode(bytes);
		}

		public LongDto(long value){
			this.value = value;
		}

	}

	@Test
	public void testBasicOutputBatching(){
		var dtos = List.of(new LongDto(1L), new LongDto(2L), new LongDto(3L));
		var lengthOfOneDto = dtos.get(2).encodeIndexed().length;

		//exception when an item doesn't fit
		Assert.assertThrows(RuntimeException.class, () -> makeBasicScanner(dtos, 0).advance());
		Assert.assertThrows(RuntimeException.class, () -> makeBasicScanner(dtos, lengthOfOneDto - 1).advance());

		//one dto fits into each advance
		List<byte[]> batchedConcatenatedByteList = makeBasicScanner(dtos, lengthOfOneDto)
				.map(ByteTool::concat)
				.list();
		Assert.assertEquals(batchedConcatenatedByteList.size(), 3);
		Assert.assertEquals(splitAndDecodeToLongDtos(batchedConcatenatedByteList), dtos);

		batchedConcatenatedByteList = makeBasicScanner(dtos, lengthOfOneDto * 2 - 1)
				.map(ByteTool::concat)
				.list();
		Assert.assertEquals(batchedConcatenatedByteList.size(), 3);
		Assert.assertEquals(splitAndDecodeToLongDtos(batchedConcatenatedByteList), dtos);

		//two dtos fit in the first list
		batchedConcatenatedByteList = makeBasicScanner(dtos, lengthOfOneDto * 2)
				.map(ByteTool::concat)
				.list();
		Assert.assertEquals(batchedConcatenatedByteList.size(), 2);
		Assert.assertEquals(splitAndDecodeToLongDtos(batchedConcatenatedByteList), dtos);

		batchedConcatenatedByteList = makeBasicScanner(dtos, lengthOfOneDto * 3 - 1)
				.map(ByteTool::concat)
				.list();
		Assert.assertEquals(batchedConcatenatedByteList.size(), 2);
		Assert.assertEquals(splitAndDecodeToLongDtos(batchedConcatenatedByteList), dtos);

		//three dtos fit in the first (and only) list
		batchedConcatenatedByteList = makeBasicScanner(dtos, lengthOfOneDto * 3)
				.map(ByteTool::concat)
				.list();
		Assert.assertEquals(batchedConcatenatedByteList.size(), 1);
		Assert.assertEquals(splitAndDecodeToLongDtos(batchedConcatenatedByteList), dtos);
	}

	@Test
	public void testWithInputBatchingAndMultiBinaryDtoEncoder(){
		var dtos = List.of(new LongDto(1L), new LongDto(2L), new LongDto(3L));
		var lengthOfOneDto = dtos.get(2).encodeIndexed().length;

		//exception when batched items don't fit in output (1 extra byte for each dto's size)
		Assert.assertThrows(
				RuntimeException.class,
				() -> makeMultiBinaryDtoEncoderScanner(dtos, lengthOfOneDto).advance());

		//one dto in each list
		List<byte[]> batchedEncodedByteList = makeMultiBinaryDtoEncoderScanner(dtos, lengthOfOneDto + 1)
				.map(ByteTool::concat)
				.list();
		Assert.assertEquals(batchedEncodedByteList.size(), 3);
		Assert.assertEquals(decodeWithBinaryDtoByteArrayScanner(batchedEncodedByteList), dtos);

		//all dtos fit in one list
		batchedEncodedByteList = makeMultiBinaryDtoEncoderScanner(dtos, (lengthOfOneDto + 1) * 3)
				.map(ByteTool::concat)
				.list();//each dto is 2 byte[]s, because of MultiBinaryDtoEncoder)
		Assert.assertEquals(batchedEncodedByteList.size(), 1);
		Assert.assertEquals(decodeWithBinaryDtoByteArrayScanner(batchedEncodedByteList), dtos);
	}

	@Test
	public void testUnbatchedAllowedOverflow(){
		var halfMaxPlusOneBytes = List.of(new byte[Integer.MAX_VALUE / 2 + 1], new byte[Integer.MAX_VALUE / 2 + 1]);
		var scanner = new BatchingByteArrayScanner(Scanner.of(halfMaxPlusOneBytes), Integer.MAX_VALUE);
		Assert.assertEquals(scanner.list().size(), 2);
	}

	@Test
	public void testOutputBatchedAllowedOverflow(){
		var halfMaxPlusOneBytes = List.of(
				new byte[Integer.MAX_VALUE / 3 + 1],
				new byte[Integer.MAX_VALUE / 3 + 1],
				new byte[Integer.MAX_VALUE / 3 + 1]);
		var scanner = new BatchingByteArrayScanner(Scanner.of(halfMaxPlusOneBytes), Integer.MAX_VALUE);
		Assert.assertEquals(scanner.list().size(), 2);
	}

	@Test
	public void testExactMaxOutputBatch(){
		int size1 = Integer.MAX_VALUE / 2;
		var arr1 = new byte[size1];
		var arr2 = new byte[Integer.MAX_VALUE - size1];
		var halfMaxBytes = List.of(arr1, arr2, arr1, arr2);
		var scanner = new BatchingByteArrayScanner(Scanner.of(halfMaxBytes), Integer.MAX_VALUE);
		Assert.assertEquals(scanner.list().size(), 2);
	}

	private BatchingByteArrayScanner makeBasicScanner(List<LongDto> dtos, int limit){
		return new BatchingByteArrayScanner(Scanner.of(dtos).map(LongDto::encodeIndexed), limit);
	}

	private BatchingByteArrayScanner makeMultiBinaryDtoEncoderScanner(List<LongDto> dtos, int limit){
		return new BatchingByteArrayScanner(
				new MultiBinaryDtoEncoder<>(LongDto.class).encodeWithConcatenatedLength(dtos),
				limit);
	}

	private List<LongDto> splitAndDecodeToLongDtos(List<byte[]> batchedConcatenatedByteList){
		return Scanner.of(batchedConcatenatedByteList)
				.concatIter(bytes -> {
					var result = new ArrayList<byte[]>();
					for(int i = 0; i < bytes.length; i += 10){
						result.add(ByteTool.copyOfRange(bytes, i, 10));
					}
					return result;
				}).map(LongDto::decode)
				.list();
	}

	private List<LongDto> decodeWithBinaryDtoByteArrayScanner(List<byte[]> inputs){
		return Scanner.of(inputs)
				.concat(bytes -> BinaryDtoByteArrayScanner.of(LongDto.class, bytes))
				.list();
	}

}

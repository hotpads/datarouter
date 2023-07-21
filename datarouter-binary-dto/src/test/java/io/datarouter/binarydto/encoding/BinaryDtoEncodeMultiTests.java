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
package io.datarouter.binarydto.encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.binarydto.multi.bytearray.BinaryDtoByteArrayScanner;
import io.datarouter.binarydto.multi.bytearray.MultiBinaryDtoEncoder;
import io.datarouter.binarydto.multi.iostream.BinaryDtoInputStreamScanner;
import io.datarouter.binarydto.multi.iostream.BinaryDtoOutputStreamWriter;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.HexBlockTool;

public class BinaryDtoEncodeMultiTests{

	private static class TestDto extends ComparableBinaryDto<TestDto>{
		@BinaryDtoField(index = 2)
		public final float cherry;
		@BinaryDtoField(index = 0)
		public final Integer banana;
		@BinaryDtoField(index = 1)
		public final String apple;//first alphabetically

		public TestDto(float cherry, Integer banana, String apple){
			this.cherry = cherry;
			this.banana = banana;
			this.apple = apple;
		}
	}

	private static final TestDto
			DTO_0 = new TestDto(1.1f, 3, "A"),
			DTO_1 = new TestDto(2.1f, 4, "BB"),
			DTO_2 = new TestDto(3.1f, 5, "CCC");

//	static{
//		HexBlockTool.print(BinaryDtoIndexedCodec.of(TestDto.class).encode(DTO_0));
//		HexBlockTool.print(BinaryDtoIndexedCodec.of(TestDto.class).encode(DTO_1));
//		HexBlockTool.print(BinaryDtoIndexedCodec.of(TestDto.class).encode(DTO_2));
//	}
	private static final String
			HEX_0 = "0004800000030101410204bf8ccccd",
			HEX_1 = "000480000004010242420204c0066666",
			HEX_2 = "00048000000501034343430204c0466666";

//	static{
//		HexBlockTool.print(VarIntTool.encode(BYTES_0.length));
//		HexBlockTool.print(VarIntTool.encode(BYTES_1.length));
//		HexBlockTool.print(VarIntTool.encode(BYTES_2.length));
//	}
	private static final String
			HEX_LENGTH_0 = "0f",
			HEX_LENGTH_1 = "10",
			HEX_LENGTH_2 = "11";

	private static final List<TestDto> DTOS = List.of(DTO_0, DTO_1, DTO_2);

//	static{
//		byte[] bytes = new MultiBinaryDtoEncoder<>(TestDto.class)
//				.encode(DTOS)
//				.listTo(ByteTool::concat);
//		HexBlockTool.print(bytes);
//	}
	private static final String HEX_BLOCK = """
			0f0004800000030101410204bf8ccccd10000480000004010242420204c006666611000480000005
			01034343430204c0466666
			""";
	private static final String HEX_ALL = HexBlockTool.trim(HEX_BLOCK);
	private static final byte[] BYTES_ALL = HexBlockTool.fromHexBlock(HEX_ALL);

	@Test
	public void testConcat(){
		String hexConcatenated = String.join(
				"",
				HEX_LENGTH_0,
				HEX_0,
				HEX_LENGTH_1,
				HEX_1,
				HEX_LENGTH_2,
				HEX_2);
		Assert.assertEquals(hexConcatenated, HEX_ALL);
	}

	@Test
	public void testArrayEncode(){
		byte[] bytes = new MultiBinaryDtoEncoder<>(TestDto.class)
				.encode(DTOS)
				.listTo(ByteTool::concat);
		Assert.assertEquals(bytes, BYTES_ALL);
	}

	@Test
	public void testArrayDecode(){
		List<TestDto> actual = BinaryDtoByteArrayScanner.of(TestDto.class, BYTES_ALL).list();
		Assert.assertEquals(actual, DTOS);
	}

	@Test
	public void testOutputStreamEncode(){
		var baos = new ByteArrayOutputStream();
		try(var dtoWriter = new BinaryDtoOutputStreamWriter<>(
				TestDto.class,
				baos)){
			DTOS.forEach(dtoWriter::write);
		}
		Assert.assertEquals(baos.toByteArray(), BYTES_ALL);
	}

	@Test
	public void testInputStreamDecode(){
		try(var dtoScanner = new BinaryDtoInputStreamScanner<>(
				TestDto.class,
				new ByteArrayInputStream(BYTES_ALL))){
			List<TestDto> actual = dtoScanner.list();
			Assert.assertEquals(actual, DTOS);
		}
	}

}

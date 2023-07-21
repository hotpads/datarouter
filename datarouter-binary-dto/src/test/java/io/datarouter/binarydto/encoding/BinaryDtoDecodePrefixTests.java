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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoComparableCodec;
import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.binarydto.fieldcodec.array.ShortArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.IntBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.ShortBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.string.Utf8BinaryDtoFieldCodec;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.HexBlockTool;

public class BinaryDtoDecodePrefixTests{

	private static final byte[] ZERO = new byte[]{0};
	private static final byte[] ONE = new byte[]{1};

	private static class TestDto extends ComparableBinaryDto<TestDto>{
		public final int f1;
		public final short[] f2;
		public final String f3;
		public final Short f4;

		public TestDto(int f1, short[] f2, String f3, Short f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	private static final int VALUE_F1 = 9;
	private static final short[] VALUE_F2 = new short[]{5, 6};
	private static final String VALUE_F3 = "hello";
	private static final Short VALUE_F4 = 7;

	private static final TestDto DTO = new TestDto(VALUE_F1, VALUE_F2, VALUE_F3, VALUE_F4);

	private static final byte[] ALL_BYTES = DTO.encodeComparable();
	static{
//		HexBlockTool.print(actualBytes);
	}
	private static final String HEX = "800000090180058006000168656c6c6f00018007";

	private static final byte[] BYTES_F1 = ByteTool.concat(
			new IntBinaryDtoFieldCodec().encode(9));
	private static final byte[] BYTES_F2 = ByteTool.concat(
			ONE,
			new ShortArrayBinaryDtoFieldCodec().encode(new short[]{5, 6}),
			ZERO);
	private static final byte[] BYTES_F3 = ByteTool.concat(
			ONE,
			new Utf8BinaryDtoFieldCodec().encode("hello"),
			ZERO);
	private static final byte[] BYTES_F4 = ByteTool.concat(
			ONE,
			new ShortBinaryDtoFieldCodec().encode((short)7));

	private static final byte[] PREFIX_1 = BYTES_F1;
	private static final byte[] PREFIX_2 = ByteTool.concat(PREFIX_1, BYTES_F2);
	private static final byte[] PREFIX_3 = ByteTool.concat(PREFIX_2, BYTES_F3);
	private static final byte[] PREFIX_4 = ByteTool.concat(PREFIX_3, BYTES_F4);

	private static final BinaryDtoComparableCodec<TestDto> CODEC = BinaryDtoComparableCodec.of(TestDto.class);

	@Test
	public void testEncoding(){
		byte[] concatenatedBytes = ByteTool.concat(BYTES_F1, BYTES_F2, BYTES_F3, BYTES_F4);
		Assert.assertEquals(ALL_BYTES, concatenatedBytes);
		byte[] hexBytes = HexBlockTool.fromHexBlock(HEX);
		Assert.assertEquals(hexBytes, concatenatedBytes);
		TestDto actual = CODEC.decode(ALL_BYTES);
		Assert.assertEquals(actual, DTO);
	}

	@Test
	public void testDecodePrefixLength(){
		byte[] bytes = CODEC.encode(DTO);

		int length1 = CODEC.decodePrefixLength(bytes, 0, 1);
		Assert.assertEquals(length1, PREFIX_1.length);
		int length2 = CODEC.decodePrefixLength(bytes, 0, 2);
		Assert.assertEquals(length2, PREFIX_2.length);
		int length3 = CODEC.decodePrefixLength(bytes, 0, 3);
		Assert.assertEquals(length3, PREFIX_3.length);
		int length4 = CODEC.decodePrefixLength(bytes, 0, 4);
		Assert.assertEquals(length4, PREFIX_4.length);
	}

	@Test
	public void testEncodePrefix(){
		Assert.assertEquals(CODEC.encodePrefix(DTO, 0), EmptyArray.BYTE);
		Assert.assertEquals(CODEC.encodePrefix(DTO, 1), PREFIX_1);
		Assert.assertEquals(CODEC.encodePrefix(DTO, 2), PREFIX_2);
		Assert.assertEquals(CODEC.encodePrefix(DTO, 3), PREFIX_3);
		Assert.assertEquals(CODEC.encodePrefix(DTO, 4), PREFIX_4);
	}

	@Test
	public void testDecodePrefix(){
		Assert.assertEquals(CODEC.decodePrefix(ALL_BYTES, 0), new TestDto(0, null, null, null));//primitive becomes 0
		Assert.assertEquals(CODEC.decodePrefix(ALL_BYTES, 1), new TestDto(VALUE_F1, null, null, null));
		Assert.assertEquals(CODEC.decodePrefix(ALL_BYTES, 2), new TestDto(VALUE_F1, VALUE_F2, null, null));
		Assert.assertEquals(CODEC.decodePrefix(ALL_BYTES, 3), new TestDto(VALUE_F1, VALUE_F2, VALUE_F3, null));
		Assert.assertEquals(CODEC.decodePrefix(ALL_BYTES, 4), new TestDto(VALUE_F1, VALUE_F2, VALUE_F3, VALUE_F4));
	}

	@Test
	public void testPrefix(){
		byte[] expectedBytes = ByteTool.concat(BYTES_F1, BYTES_F2);
		byte[] actualBytes = CODEC.encodePrefix(DTO, 2);
		Assert.assertEquals(actualBytes, expectedBytes);

		TestDto actual = CODEC.decodePrefix(actualBytes, 2);
		Assert.assertEquals(actual.f1, 9);
		Assert.assertEquals(actual.f2, new short[]{5, 6});
		Assert.assertNull(actual.f3);
		Assert.assertNull(actual.f4);
	}

}

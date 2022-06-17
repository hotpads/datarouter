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
package io.datarouter.bytes.binarydto;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.binarydto.codec.BinaryDtoComparableCodec;
import io.datarouter.bytes.binarydto.dto.ComparableBinaryDto;
import io.datarouter.bytes.binarydto.fieldcodec.array.ShortArrayBinaryDtoFieldCodec;
import io.datarouter.bytes.binarydto.fieldcodec.primitive.IntBinaryDtoFieldCodec;
import io.datarouter.bytes.binarydto.fieldcodec.primitive.ShortBinaryDtoFieldCodec;
import io.datarouter.bytes.binarydto.fieldcodec.string.Utf8BinaryDtoFieldCodec;
import io.datarouter.bytes.codec.bytestringcodec.CsvIntByteStringCodec;

public class BinaryDtoDecodePrefixTests{

	private static final byte[] ZERO = new byte[]{0};
	private static final byte[] ONE = new byte[]{1};

	public static class TestDto extends ComparableBinaryDto<TestDto>{
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

	private static final TestDto DTO = new TestDto(
			9,
			new short[]{5, 6},
			"hello",
			(short)7);

	private static final byte[] BYTES_F1 = ByteTool.concat(new IntBinaryDtoFieldCodec().encode(9));
	private static final byte[] BYTES_F2 = ByteTool.concat(
			ONE,
			new ShortArrayBinaryDtoFieldCodec().encode(new short[]{5, 6}), ZERO);
	private static final byte[] BYTES_F3 = ByteTool.concat(ONE, new Utf8BinaryDtoFieldCodec().encode("hello"), ZERO);
	private static final byte[] BYTES_F4 = ByteTool.concat(ONE, new ShortBinaryDtoFieldCodec().encode((short)7));
	static{
		Console.println(CsvIntByteStringCodec.INSTANCE.encode(BYTES_F1));
		Console.println(CsvIntByteStringCodec.INSTANCE.encode(BYTES_F2));
		Console.println(CsvIntByteStringCodec.INSTANCE.encode(BYTES_F3));
		Console.println(CsvIntByteStringCodec.INSTANCE.encode(BYTES_F4));
	}

	private static final BinaryDtoComparableCodec<TestDto> CODEC = BinaryDtoComparableCodec.of(TestDto.class);

	@Test
	public void testEncoding(){
		byte[] expectedBytes = ByteTool.concat(BYTES_F1, BYTES_F2, BYTES_F3, BYTES_F4);
		byte[] actualBytes = CODEC.encode(DTO);
		Console.println(CsvIntByteStringCodec.INSTANCE.encode(actualBytes));
		Assert.assertEquals(actualBytes, expectedBytes);

		TestDto actual = CODEC.decode(actualBytes);
		Assert.assertEquals(actual, DTO);
	}

	@Test
	public void testPrefixLength(){
		byte[] bytes = CODEC.encode(DTO);

		int length1 = CODEC.decodePrefixLength(bytes, 0, 1);
		Assert.assertEquals(length1, BYTES_F1.length);
		int length2 = CODEC.decodePrefixLength(bytes, 0, 2);
		Assert.assertEquals(length2, length1 + BYTES_F2.length);
		int length3 = CODEC.decodePrefixLength(bytes, 0, 3);
		Assert.assertEquals(length3, length2 + BYTES_F3.length);
		int length4 = CODEC.decodePrefixLength(bytes, 0, 4);
		Assert.assertEquals(length4, length3 + BYTES_F4.length);
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

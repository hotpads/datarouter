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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.binarydto.BinaryDtoFieldConverterTests.TestEnum.TestEnumBinaryDtoIntCodec;
import io.datarouter.bytes.binarydto.BinaryDtoFieldConverterTests.TestEnum.TestEnumBinaryDtoPrefixedStringCodec;
import io.datarouter.bytes.binarydto.BinaryDtoFieldConverterTests.TestEnum.TestEnumBinaryDtoStringCodec;
import io.datarouter.bytes.binarydto.BinaryDtoFieldConverterTests.TestEnum.TestEnumBinaryDtoVarIntCodec;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BinaryDto;
import io.datarouter.bytes.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoConvertingFieldCodec;
import io.datarouter.bytes.binarydto.fieldcodec.primitive.IntBinaryDtoFieldCodec;
import io.datarouter.bytes.binarydto.fieldcodec.primitive.VarIntBinaryDtoFieldCodec;
import io.datarouter.bytes.binarydto.fieldcodec.string.PrefixedUtf8BinaryDtoFieldCodec;
import io.datarouter.bytes.binarydto.fieldcodec.string.TerminatedUtf8BinaryDtoFieldCodec;
import io.datarouter.scanner.Scanner;

public class BinaryDtoFieldConverterTests{

	public static enum TestEnum{
		AA(5, "PA"),
		BB(6, "PB"),
		CC(7, "PC");

		public final int encodedInt;
		public final String encodedString;

		private TestEnum(int encodedInt, String encodedString){
			this.encodedInt = encodedInt;
			this.encodedString = encodedString;
		}

		/*---------- int converters -------------*/

		private static final Map<Integer,TestEnum> BY_INT = Scanner.of(values())
				.toMap(value -> value.encodedInt);

		public static class TestEnumBinaryDtoIntCodec extends BinaryDtoConvertingFieldCodec<TestEnum,Integer>{
			public TestEnumBinaryDtoIntCodec(){
				super(value -> value.encodedInt, BY_INT::get, new IntBinaryDtoFieldCodec());
			}
		}

		public static class TestEnumBinaryDtoVarIntCodec extends BinaryDtoConvertingFieldCodec<TestEnum,Integer>{
			public TestEnumBinaryDtoVarIntCodec(){
				super(value -> value.encodedInt, BY_INT::get, new VarIntBinaryDtoFieldCodec());
			}
		}

		/*---------- string converters -------------*/

		private static final Map<String,TestEnum> BY_STRING = Scanner.of(values())
				.toMap(value -> value.encodedString);

		public static class TestEnumBinaryDtoStringCodec extends BinaryDtoConvertingFieldCodec<TestEnum,String>{
			public TestEnumBinaryDtoStringCodec(){
				super(value -> value.encodedString, BY_STRING::get, new TerminatedUtf8BinaryDtoFieldCodec());
			}
		}

		public static class TestEnumBinaryDtoPrefixedStringCodec
		extends BinaryDtoConvertingFieldCodec<TestEnum,String>{
			public TestEnumBinaryDtoPrefixedStringCodec(){
				super(value -> value.encodedString, BY_STRING::get, new PrefixedUtf8BinaryDtoFieldCodec());
			}
		}

	}

	public static class TestDto extends BinaryDto<TestDto>{

		//convert to default (comparable 4 byte) int
		@BinaryDtoField(codec = TestEnumBinaryDtoIntCodec.class)
		public final TestEnum f1;

		//convert to default comparable utf8 string with terminator
		@BinaryDtoField(codec = TestEnumBinaryDtoStringCodec.class)
		public final TestEnum f2;

		//serialize default enum.name() to comparable ascii bytes with terminator
		public final TestEnum f3;

		//test converter on null field value
		@BinaryDtoField(codec = TestEnumBinaryDtoStringCodec.class)
		public final TestEnum f4;

		//convert to int, encode as VarInt
		@BinaryDtoField(codec = TestEnumBinaryDtoVarIntCodec.class)
		public final TestEnum f5;

		//ensure each item encoded as VarInt
		@BinaryDtoField(codec = TestEnumBinaryDtoVarIntCodec.class)
		public final TestEnum[] f6;

		//ensure each item encoded as PrefixedString
		@BinaryDtoField(codec = TestEnumBinaryDtoPrefixedStringCodec.class)
		public final List<TestEnum> f7;

		public TestDto(
				TestEnum f1,
				TestEnum f2,
				TestEnum f3,
				TestEnum f4,
				TestEnum f5,
				TestEnum[] f6,
				List<TestEnum> f7){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
			this.f5 = f5;
			this.f6 = f6;
			this.f7 = f7;
		}
	}

	@Test
	public void testEncoding(){
		var codec = BinaryDtoCodec.of(TestDto.class);
		var dto = new TestDto(
				TestEnum.AA,
				TestEnum.BB,
				TestEnum.CC,
				null,
				TestEnum.BB,
				new TestEnum[]{TestEnum.BB, null, TestEnum.CC},
				Arrays.asList(TestEnum.CC, null, TestEnum.BB));
		byte[] expectedBytes = {
				//f1
				1,//present
				Byte.MIN_VALUE, 0, 0, 5,//int value
				//f2
				1,//present
				'P', 'B', 0,//encodedString with terminator
				//f3
				1,//present
				'C', 'C', 0,//enum.name() value with terminator
				//f4
				0,//null
				//f5
				1,//present
				6,//encoded VarInt
				//f6
				1,//present
				3,//size
				1,//item0 present
				6,//item0 varint value
				0,//item1 null
				1,//item2 present
				7,//item2 varint value
				//f7
				1,//present
				3,//size
				1,//item0 present
				2, 'P', 'C',//item0 prefixed string value
				0,//item1 null
				1,//item2 present
				2, 'P', 'B'};//item2 prefixed string value
		byte[] actualBytes = codec.encode(dto);
		Assert.assertEquals(actualBytes, expectedBytes);

		TestDto actual = codec.decode(actualBytes);
		Assert.assertEquals(actual, dto);
	}


}

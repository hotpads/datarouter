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
package io.datarouter.binarydto.migration;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.HexBlockTool;

public class BinaryDtoFieldMigrationTests{

	private static class InnerDto extends BinaryDto<InnerDto>{
		@BinaryDtoField(index = 0)
		public final int innerF0;

		public InnerDto(int innerF0){
			this.innerF0 = innerF0;
		}
	}

	private static class FirstOuterDto extends BinaryDto<FirstOuterDto>{
		@BinaryDtoField(index = 0)
		public final String outerF0;
		@BinaryDtoField(index = 1)
		public final List<InnerDto> outerF1;
		@BinaryDtoField(index = 2)
		public final String outerF2;

		public FirstOuterDto(String outerF0, List<InnerDto> outerF1, String outerF2){
			this.outerF0 = outerF0;
			this.outerF1 = outerF1;
			this.outerF2 = outerF2;
		}
	}

	/**
	 * Create a second class with fields missing.
	 * In normal usage there would not be a separate class - it would be the original class without the field.
	 */
	private static class SecondOuterDto extends BinaryDto<SecondOuterDto>{
		@BinaryDtoField(index = 0)
		public final String outerF0;
		//removed outerF1 at index=1
		@BinaryDtoField(index = 2)
		public final String outerF2;

		public SecondOuterDto(String outerF0, String outerF2){
			this.outerF0 = outerF0;
			this.outerF2 = outerF2;
		}
	}

	private static final FirstOuterDto FIRST = new FirstOuterDto(
			"hello",
			List.of(new InnerDto(7), new InnerDto(8)),
			"goodbye");
	private static final SecondOuterDto SECOND = new SecondOuterDto(
			"hello",
			"goodbye");
	private static final FirstOuterDto FIRST_FROM_SECOND = new FirstOuterDto(
			"hello",
			null,
			"goodbye");

//	static{
//		HexBlockTool.print(FIRST.encodeIndexed());
//		HexBlockTool.print(SECOND.encodeIndexed());
//		HexBlockTool.print(FIRST_FROM_SECOND.encodeIndexed());
//	}
	private static final String FIRST_HEX = """
			000568656c6c6f011102010600048000000701060004800000080207676f6f64627965
			""";
	private static final String SECOND_HEX = """
			000568656c6c6f0207676f6f64627965
			""";
	private static final String FIRST_FROM_SECOND_HEX = """
			000568656c6c6f0207676f6f64627965
			""";

	private static byte[] FIRST_BYTES = HexBlockTool.fromHexBlock(FIRST_HEX);
	private static byte[] SECOND_BYTES = HexBlockTool.fromHexBlock(SECOND_HEX);
	private static byte[] FIRST_FROM_SECOND_BYTES = HexBlockTool.fromHexBlock(FIRST_FROM_SECOND_HEX);

	@Test
	public void testHex(){
		Assert.assertNotEquals(FIRST_BYTES, SECOND_BYTES);
		Assert.assertEquals(SECOND_BYTES, FIRST_FROM_SECOND_BYTES);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(FirstOuterDto.class).decode(FIRST_BYTES),
				FIRST);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(SecondOuterDto.class).decode(SECOND_BYTES),
				SECOND);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(FirstOuterDto.class).decode(FIRST_FROM_SECOND_BYTES),
				FIRST_FROM_SECOND);
	}

	/**
	 * Simulates removing a field from the code.
	 * The second DTO should be able to parse the binary output of the first dto.
	 * The bytes from the List field should be skipped over, and later fields parsed correctly.
	 */
	@Test
	public void testSecondFromFirstBytes(){
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(SecondOuterDto.class).decode(FIRST_BYTES),
				SECOND);
	}

	/**
	 * Simulates adding a field to the code.
	 * The first DTO should be able to parse the binary output of the second dto.
	 * The List field should be null.
	 */
	@Test
	public void testFirstFromSecondBytes(){
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(FirstOuterDto.class).decode(FIRST_FROM_SECOND_BYTES),
				FIRST_FROM_SECOND);
	}

}

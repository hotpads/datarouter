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

public class BinaryDtoNestedFieldMigrationTests{

	public static class FirstInnerDto extends BinaryDto<FirstInnerDto>{
		@BinaryDtoField(index = 0)
		public final int innerF0;

		public FirstInnerDto(int innerF0){
			this.innerF0 = innerF0;
		}
	}

	/**
	 * Create a second class with fields added.
	 * In normal usage there would not be a separate class - it would be the original class with the new field.
	 */
	public static class SecondInnerDto extends BinaryDto<SecondInnerDto>{
		@BinaryDtoField(index = 0)
		public final int innerF0;
		@BinaryDtoField(index = 1)
		public final String innerF1;

		public SecondInnerDto(int innerF0, String innerF1){
			this.innerF0 = innerF0;
			this.innerF1 = innerF1;
		}
	}

	public static class FirstOuterDto extends BinaryDto<FirstOuterDto>{
		@BinaryDtoField(index = 0)
		public final String outerF0;
		@BinaryDtoField(index = 1)
		public final List<FirstInnerDto> outerF1;

		public FirstOuterDto(String outerF0, List<FirstInnerDto> outerF1){
			this.outerF0 = outerF0;
			this.outerF1 = outerF1;
		}
	}

	/**
	 * Same as FirstOuterDto but with a different nested DTO.
	 */
	public static class SecondOuterDto extends BinaryDto<SecondOuterDto>{
		@BinaryDtoField(index = 0)
		public final String outerF0;
		@BinaryDtoField(index = 1)
		public final List<SecondInnerDto> outerF1;

		public SecondOuterDto(String outerF0, List<SecondInnerDto> outerF1){
			this.outerF0 = outerF0;
			this.outerF1 = outerF1;
		}
	}

	private static final FirstOuterDto FIRST = new FirstOuterDto(
			"hello",
			List.of(new FirstInnerDto(7), new FirstInnerDto(8)));
	private static final SecondOuterDto SECOND = new SecondOuterDto(
			"hello",
			List.of(new SecondInnerDto(7, "hi"), new SecondInnerDto(8, "bye")));
	private static final SecondOuterDto SECOND_FROM_FIRST = new SecondOuterDto(
			"hello",
			List.of(new SecondInnerDto(7, null), new SecondInnerDto(8, null)));

//	static{
//		HexBlockTool.print(FIRST.encodeIndexed());
//		HexBlockTool.print(SECOND.encodeIndexed());
//		HexBlockTool.print(SECOND_FROM_FIRST.encodeIndexed());
//	}
	private static final String FIRST_HEX = """
			000568656c6c6f01110201060004800000070106000480000008
			""";
	private static final String SECOND_HEX = """
			000568656c6c6f011a02010a00048000000701026869010b0004800000080103627965
			""";
	private static final String SECOND_FROM_FIRST_HEX = """
			000568656c6c6f01110201060004800000070106000480000008
			""";

	private static byte[] FIRST_BYTES = HexBlockTool.fromHexBlock(FIRST_HEX);
	private static byte[] SECOND_BYTES = HexBlockTool.fromHexBlock(SECOND_HEX);
	private static byte[] SECOND_FROM_FIRST_BYTES = HexBlockTool.fromHexBlock(SECOND_FROM_FIRST_HEX);

	@Test
	public void testHex(){
		Assert.assertNotEquals(FIRST_BYTES, SECOND_BYTES);
		Assert.assertEquals(FIRST_BYTES, SECOND_FROM_FIRST_BYTES);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(FirstOuterDto.class).decode(FIRST_BYTES),
				FIRST);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(SecondOuterDto.class).decode(SECOND_BYTES),
				SECOND);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(SecondOuterDto.class).decode(SECOND_FROM_FIRST_BYTES),
				SECOND_FROM_FIRST);
	}

	/**
	 * Simulates adding a field to the code.
	 * The second DTO should be able to parse the binary output of the first dto.
	 * The nested field should be null.
	 */
	@Test
	public void testForwardCompatibility(){
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(SecondOuterDto.class).decode(FIRST_BYTES),
				SECOND_FROM_FIRST);
	}

	/**
	 * Simulates removing a field from the code.
	 * The first DTO should be able to parse the binary output of the second dto.
	 * The bytes from the nested field should be skipped over, and later fields parsed correctly.
	 */
	@Test
	public void testBackwardCompatibility(){
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(FirstOuterDto.class).decode(SECOND_BYTES),
				FIRST);
	}

}

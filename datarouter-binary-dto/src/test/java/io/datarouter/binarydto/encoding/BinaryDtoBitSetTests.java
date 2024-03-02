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

import java.util.Arrays;
import java.util.BitSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.bytes.HexBlockTool;

public class BinaryDtoBitSetTests{

	private static class TestDto extends ComparableBinaryDto<TestDto>{
		@SuppressWarnings("unused")
		final BitSet s1;

		public TestDto(BitSet s1){
			this.s1 = s1;
		}
	}

	private static final TestDto DTO_NULL_BITS = new TestDto(null);
	private static final TestDto DTO_EMTPY_BITS = new TestDto(make());
	private static final TestDto DTO_SINGLE_BIT = new TestDto(make(0));
	private static final TestDto DTO_MULTI_BYTES = new TestDto(make(1, 9));

	@Test
	public void testDecodeBehavior(){
		// BitSet doesn't allocate anything for empty input.
		Assert.assertEquals(BitSet.valueOf(new byte[]{}).size(), 0);
		// BitSet accepts empty bytes, but trims them down to nothing.
		Assert.assertEquals(BitSet.valueOf(new byte[]{0}).size(), 0);
		// When even one low level bit is set, it internally allocates a long, giving size=64
		Assert.assertEquals(BitSet.valueOf(new byte[]{1}).size(), 64);// Size is a multiple of 64
	}

	@Test
	public void testNullBits(){
//		HexBlockTool.print(DTO_NULL_BITS.encodeIndexed());
		String hex = "";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO_NULL_BITS.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO_NULL_BITS);
	}

	@Test
	public void testEmptyBits(){
//		HexBlockTool.print(DTO_EMTPY_BITS.encodeIndexed());
		String hex = "0000";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO_EMTPY_BITS.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO_EMTPY_BITS);
	}

	@Test
	public void testSingleBit(){
//		HexBlockTool.print(DTO_SINGLE_BIT.encodeIndexed());
		String hex = "000101";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO_SINGLE_BIT.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO_SINGLE_BIT);
	}

	@Test
	public void testMultiBytes(){
//		HexBlockTool.print(DTO_MULTI_BYTES.encodeIndexed());
		String hex = "00020202";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO_MULTI_BYTES.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO_MULTI_BYTES);
	}

	private static BitSet make(int... bitsToSet){
		var bitSet = new BitSet();
		Arrays.stream(bitsToSet).forEach(bitSet::set);
		return bitSet;
	}

}

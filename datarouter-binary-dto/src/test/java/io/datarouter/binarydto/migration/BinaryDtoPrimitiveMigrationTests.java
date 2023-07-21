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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.HexBlockTool;

public class BinaryDtoPrimitiveMigrationTests{

	public static class FirstDto extends BinaryDto<FirstDto>{
		@BinaryDtoField(index = 0)
		public final int innerF0;

		public FirstDto(int innerF0){
			this.innerF0 = innerF0;
		}
	}

	public static class SecondDto extends BinaryDto<SecondDto>{
		@BinaryDtoField(index = 0)
		public final int innerF0;
		@BinaryDtoField(index = 1)
		public final int innerF1;

		public SecondDto(int innerF0, int innerF1){
			this.innerF0 = innerF0;
			this.innerF1 = innerF1;
		}
	}


	private static final FirstDto FIRST = new FirstDto(7);
	private static final SecondDto SECOND = new SecondDto(7, 8);
	private static final SecondDto SECOND_FROM_FIRST = new SecondDto(7, 0);//missing field gets default java value

//	static{
//		HexBlockTool.print(FIRST.encodeIndexed());
//		HexBlockTool.print(SECOND.encodeIndexed());
//	}
	private static final String FIRST_HEX = "000480000007";
	private static final String SECOND_HEX = "000480000007010480000008";

	private static final byte[] FIRST_BYTES = HexBlockTool.fromHexBlock(FIRST_HEX);
	private static final byte[] SECOND_BYTES = HexBlockTool.fromHexBlock(SECOND_HEX);

	@Test
	public void testHex(){
		Assert.assertEquals(FIRST.encodeIndexed(), FIRST_BYTES);
		Assert.assertEquals(SECOND.encodeIndexed(), SECOND_BYTES);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(FirstDto.class).decode(FIRST_BYTES), FIRST);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(SecondDto.class).decode(SECOND_BYTES), SECOND);
	}

	@Test
	public void testCompatibility(){
		// FirstDto should ignore the extra bytes
		Assert.assertEquals(BinaryDtoIndexedCodec.of(FirstDto.class).decode(SECOND_BYTES), FIRST);
		// SecondDto will leave the primitive at it's default value when it's missing from the bytes
		Assert.assertEquals(BinaryDtoIndexedCodec.of(SecondDto.class).decode(FIRST_BYTES), SECOND_FROM_FIRST);
	}

}

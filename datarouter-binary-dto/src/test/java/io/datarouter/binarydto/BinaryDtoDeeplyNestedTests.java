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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.HexBlockTool;
import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class BinaryDtoDeeplyNestedTests{

	private static class Level0Dto extends BinaryDto<Level0Dto>{
		@BinaryDtoField(index = 0)
		public final Level1Dto level1Dto;

		public Level0Dto(Level1Dto level1Dto){
			this.level1Dto = level1Dto;
		}
	}

	private static class Level1Dto extends BinaryDto<Level1Dto>{
		@BinaryDtoField(index = 0)
		public final Level2Dto level2Dto;

		public Level1Dto(Level2Dto level2Dto){
			this.level2Dto = level2Dto;
		}
	}

	private static class Level2Dto extends BinaryDto<Level2Dto>{
		@BinaryDtoField(index = 0)
		public final Level3Dto level3Dto;
		@BinaryDtoField(index = 1)
		public final byte[] bytes;

		public Level2Dto(Level3Dto level3Dto, byte[] bytes){
			this.level3Dto = level3Dto;
			this.bytes = bytes;
		}
	}

	private static class Level3Dto extends BinaryDto<Level3Dto>{
		@BinaryDtoField(index = 0)
		public final byte[] bytes;

		public Level3Dto(byte[] bytes){
			this.bytes = bytes;
		}
	}

	private static final String THREE = "three";
	private static final String TWO = "two";

	private static final Level3Dto LEVEL_3_DTO = new Level3Dto(StringCodec.UTF_8.encode(THREE));
	private static final Level2Dto LEVEL_2_DTO = new Level2Dto(LEVEL_3_DTO, StringCodec.UTF_8.encode(TWO));
	private static final Level1Dto LEVEL_1_DTO = new Level1Dto(LEVEL_2_DTO);
	private static final Level0Dto LEVEL_0_DTO = new Level0Dto(LEVEL_1_DTO);

	@Test
	public void testEquals(){
		// Different object with the same bytes.  BinaryDto should consider it equal.
		var anotherLevel3Dto = new Level3Dto(StringCodec.UTF_8.encode(THREE));
		Assert.assertEquals(anotherLevel3Dto, LEVEL_3_DTO);
		Assert.assertEquals(anotherLevel3Dto.hashCode(), LEVEL_3_DTO.hashCode());

		var anotherLevel2Dto = new Level2Dto(anotherLevel3Dto, StringCodec.UTF_8.encode(TWO));
		Assert.assertEquals(anotherLevel2Dto, LEVEL_2_DTO);
		Assert.assertEquals(anotherLevel2Dto.hashCode(), LEVEL_2_DTO.hashCode());

		var anotherLevel1Dto = new Level1Dto(anotherLevel2Dto);
		Assert.assertEquals(anotherLevel1Dto, LEVEL_1_DTO);
		Assert.assertEquals(anotherLevel1Dto.hashCode(), LEVEL_1_DTO.hashCode());

		var anotherLevel0Dto = new Level0Dto(anotherLevel1Dto);
		Assert.assertEquals(anotherLevel0Dto, LEVEL_0_DTO);
		Assert.assertEquals(anotherLevel0Dto.hashCode(), LEVEL_0_DTO.hashCode());
	}

	@Test
	public void testNotEquals(){
		// Different bytes value.  All levels should report not equals.
		var anotherLevel3Dto = new Level3Dto(StringCodec.UTF_8.encode("bogus"));
		Assert.assertNotEquals(anotherLevel3Dto, LEVEL_3_DTO);
		Assert.assertNotEquals(anotherLevel3Dto.hashCode(), LEVEL_3_DTO.hashCode());

		var anotherLevel2Dto = new Level2Dto(anotherLevel3Dto, StringCodec.UTF_8.encode(TWO));
		Assert.assertNotEquals(anotherLevel2Dto, LEVEL_2_DTO);
		Assert.assertNotEquals(anotherLevel2Dto.hashCode(), LEVEL_2_DTO.hashCode());

		var anotherLevel1Dto = new Level1Dto(anotherLevel2Dto);
		Assert.assertNotEquals(anotherLevel1Dto, LEVEL_1_DTO);
		Assert.assertNotEquals(anotherLevel1Dto.hashCode(), LEVEL_1_DTO.hashCode());

		var anotherLevel0Dto = new Level0Dto(anotherLevel1Dto);
		Assert.assertNotEquals(anotherLevel0Dto, LEVEL_0_DTO);
		Assert.assertNotEquals(anotherLevel0Dto.hashCode(), LEVEL_0_DTO.hashCode());
	}

	@Test
	public void testHex(){
//		HexBlockTool.print(LEVEL_3_DTO.encodeIndexed());
//		HexBlockTool.print(LEVEL_2_DTO.encodeIndexed());
//		HexBlockTool.print(LEVEL_1_DTO.encodeIndexed());
//		HexBlockTool.print(LEVEL_0_DTO.encodeIndexed());
		String level3Hex = "00057468726565";
		String level2Hex = "000700057468726565010374776f";
		String level1Hex = "000e000700057468726565010374776f";
		String level0Hex = "0010000e000700057468726565010374776f";
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(Level3Dto.class).decode(HexBlockTool.fromHexBlock(level3Hex)),
				LEVEL_3_DTO);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(Level2Dto.class).decode(HexBlockTool.fromHexBlock(level2Hex)),
				LEVEL_2_DTO);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(Level1Dto.class).decode(HexBlockTool.fromHexBlock(level1Hex)),
				LEVEL_1_DTO);
		Assert.assertEquals(
				BinaryDtoIndexedCodec.of(Level0Dto.class).decode(HexBlockTool.fromHexBlock(level0Hex)),
				LEVEL_0_DTO);
	}

}

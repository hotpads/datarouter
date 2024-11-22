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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.binarydto.internal.BinaryDtoFieldCache;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.HexBlockTool;

public class BinaryDtoRecursionTests{
	private static final Logger logger = LoggerFactory.getLogger(BinaryDtoRecursionTests.class);

	private static class TreeDto extends BinaryDto<TreeDto>{
		@BinaryDtoField(index = 0)
		public final String name;
		@BinaryDtoField(index = 1)
		public final List<TreeDto> children;

		public TreeDto(String name, List<TreeDto> children){
			this.name = name;
			this.children = children;
		}
	}

	private static final Codec<TreeDto,byte[]> CODEC = BinaryDtoIndexedCodec.of(TreeDto.class);

	@Test
	public void test(){
		var leaf0 = new TreeDto("leaf0", List.of());
		var leaf1 = new TreeDto("leaf1", List.of());
		var leaf2 = new TreeDto("leaf2", List.of());
		var leaf3 = new TreeDto("leaf3", List.of());
		var branch0 = new TreeDto("branch0", List.of(leaf0, leaf1));
		var branch1 = new TreeDto("branch1", List.of(leaf2, leaf3));
		var root = new TreeDto("root", List.of(branch0, branch1));
		Assert.assertEquals(root, CODEC.encodeAndDecode(root));
		Assert.assertEquals(BinaryDtoFieldCache.invocationCountForClass(TreeDto.class), 1);

//		HexBlockTool.print(CODEC.encode(root));
		String hex = """
				0004726f6f74014d02012400076272616e636830011902010a00056c65616630010100010a00056c
				65616631010100012400076272616e636831011902010a00056c65616632010100010a00056c6561
				6633010100""";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);

		TreeDto output = CODEC.decode(bytes);
		logger.info("{}", output);
		Assert.assertEquals(BinaryDtoFieldCache.invocationCountForClass(TreeDto.class), 1);

		Assert.assertEquals(output.children.get(0), branch0);
		Assert.assertEquals(output.children.get(0).children.get(0), leaf0);
		Assert.assertEquals(output.children.get(0).children.get(1), leaf1);
		Assert.assertEquals(output.children.get(1), branch1);
		Assert.assertEquals(output.children.get(1).children.get(0), leaf2);
		Assert.assertEquals(output.children.get(1).children.get(1), leaf3);
	}

}
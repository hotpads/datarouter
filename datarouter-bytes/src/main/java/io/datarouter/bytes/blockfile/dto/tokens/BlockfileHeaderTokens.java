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
package io.datarouter.bytes.blockfile.dto.tokens;

import java.util.List;

import io.datarouter.bytes.blockfile.dto.BlockfileTokens;
import io.datarouter.bytes.blockfile.enums.BlockfileSection;
import io.datarouter.bytes.blockfile.write.BlockfileWriter;

public record BlockfileHeaderTokens(
		byte[] length,
		byte[] value)
implements BlockfileTokens{

	@Override
	public BlockfileSection section(){
		return BlockfileSection.HEADER;
	}

	@Override
	public int totalLength(){
		return length.length + BlockfileWriter.NUM_SECTION_BYTES + value.length;
	}

	@Override
	public List<byte[]> toList(){
		return List.of(length, section().codeBytes, value);
	}

}

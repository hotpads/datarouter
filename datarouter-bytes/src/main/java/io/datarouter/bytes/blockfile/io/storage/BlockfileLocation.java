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
package io.datarouter.bytes.blockfile.io.storage;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.varint.VarIntTool;

public record BlockfileLocation(
		long from,
		int length){

	public long to(){
		return from + length;
	}

	public static final Codec<BlockfileLocation,byte[]> CODEC = Codec.of(
			BlockfileLocation::encodeVarInt,
			BlockfileLocation::decodeVarInt);

	private static byte[] encodeVarInt(BlockfileLocation location){
		return ByteTool.concat(
				VarIntTool.encode(location.from()),
				VarIntTool.encode(location.length()));
	}

	private static BlockfileLocation decodeVarInt(byte[] bytes){
		int cursor = 0;
		long from = VarIntTool.decodeLong(bytes, cursor);
		cursor += VarIntTool.length(from);
		int length = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(length);
		return new BlockfileLocation(from, length);
	}

}

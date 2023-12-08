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
package io.datarouter.bytes.blockfile.section;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.blockfile.dto.BlockfileTokens;
import io.datarouter.bytes.blockfile.dto.tokens.BlockfileTrailerTokens;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;

public record BlockfileTrailer(
		int headerBlockLength,
		int footerBlockLength){

	public BlockfileTokens encode(){
		byte[] value = ByteTool.concat(
				RawIntCodec.INSTANCE.encode(headerBlockLength),
				RawIntCodec.INSTANCE.encode(footerBlockLength));
		return new BlockfileTrailerTokens(value);
	}

	public static BlockfileTrailer decode(byte[] bytes){
		int cursor = 0;
		int headerBlockLength = RawIntCodec.INSTANCE.decode(bytes, cursor);
		cursor += RawIntCodec.INSTANCE.length();
		int footerBlockLength = RawIntCodec.INSTANCE.decode(bytes, cursor);
		cursor += RawIntCodec.INSTANCE.length();
		return new BlockfileTrailer(headerBlockLength, footerBlockLength);
	}

}

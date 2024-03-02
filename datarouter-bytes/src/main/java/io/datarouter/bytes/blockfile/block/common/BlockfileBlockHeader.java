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
package io.datarouter.bytes.blockfile.block.common;

import java.io.InputStream;

import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.io.InputStreamTool;

public record BlockfileBlockHeader(
		int length,
		BlockfileBlockType blockType){

	public static final int LENGTH = 5;

	public int fullBlockLength(){
		return LENGTH + length;
	}

	/*--------- encode --------*/

	public int encode(byte[] bytes, int offset){
		int cursor = offset;
		cursor += RawIntCodec.INSTANCE.encode(length, bytes, cursor);
		bytes[cursor] = blockType.codeByte;
		++cursor;
		return LENGTH;
	}

	public void encode(byte[] bytes){
		encode(bytes, 0);
	}

	public byte[] encode(){
		byte[] bytes = new byte[LENGTH];
		encode(bytes);
		return bytes;
	}

	/*---------- decode ---------*/

	public static BlockfileBlockHeader decode(byte[] bytes, int offset){
		int cursor = offset;
		int length = RawIntCodec.INSTANCE.decode(bytes, offset);
		cursor += RawIntCodec.INSTANCE.length();
		BlockfileBlockType blockType = BlockfileBlockType.decode(bytes[cursor]);
		++cursor;
		return new BlockfileBlockHeader(length, blockType);
	}

	public static BlockfileBlockHeader decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public static BlockfileBlockHeader decode(InputStream inputStream){
		byte[] bytes = InputStreamTool.readNBytes(inputStream, LENGTH);
		return decode(bytes);
	}

}

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
package io.datarouter.bytes.codec.intcodec;

import io.datarouter.bytes.VarIntTool;

public class VarIntCodec{

	public static final VarIntCodec INSTANCE = new VarIntCodec();

	public int length(int value){
		return VarIntTool.length(value);
	}

	public byte[] encode(int value){
		return VarIntTool.encode(value);
	}

	public int encode(int value, byte[] bytes, int offset){
		byte[] tempBytes = VarIntTool.encode(value);
		System.arraycopy(tempBytes, 0, bytes, offset, tempBytes.length);
		return tempBytes.length;
	}

	public int decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public int decode(byte[] bytes, int offset){
		return VarIntTool.decodeInt(bytes, offset);
	}

}

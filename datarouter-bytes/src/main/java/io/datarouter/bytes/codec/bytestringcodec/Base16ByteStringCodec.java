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
package io.datarouter.bytes.codec.bytestringcodec;

public class Base16ByteStringCodec implements ByteStringCodec{

	public static final Base16ByteStringCodec INSTANCE = new Base16ByteStringCodec();

	@Override
	public String encode(byte[] bytes){
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for(byte word : bytes){
			String hex = Integer.toHexString(word & 0xff);
			if(hex.length() == 1){
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

}

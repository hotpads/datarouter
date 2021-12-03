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

public class Base2ByteStringCodec implements ByteStringCodec{

	public static final Base2ByteStringCodec INSTANCE = new Base2ByteStringCodec();

	@Override
	public String encode(byte[] bytes){
		if(bytes == null){//TODO remove null handling
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for(int n = 0; n < bytes.length; ++n){
			for(int i = 7; i >= 0; --i){
				sb.append(bytes[n] >> i & 1);
			}
		}
		return sb.toString();
	}

}

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

import java.util.Base64;

public class Base64ByteStringCodec implements ByteStringCodec{

	public static final Base64ByteStringCodec INSTANCE = new Base64ByteStringCodec();

	@Override
	public String encode(byte[] bytes){
		return Base64.getEncoder().encodeToString(bytes);
	}

	public byte[] decode(String value){
		return Base64.getDecoder().decode(value);
	}

	//this is not intended to return the actual decoded length, since padding is not considered.
	public static int getMaxByteLength(int base64EncodedLength){
		return base64EncodedLength * 3 / 4;//Base64 uses 4 characters per 3 bytes
	}

}

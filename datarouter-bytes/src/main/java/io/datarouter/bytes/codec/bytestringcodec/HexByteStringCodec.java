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

import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class HexByteStringCodec implements ByteStringCodec{

	public static final HexByteStringCodec INSTANCE = new HexByteStringCodec();

	private static final byte[] HEX_DIGITS = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	@Override
	public String encode(byte[] bytes){
		byte[] hexBytes = toHexBytes(bytes);
		return StringCodec.US_ASCII.decode(hexBytes);
	}

	private static byte[] toHexBytes(byte[] bytes){
		byte[] hexBytes = new byte[2 * bytes.length];
		int cursor = 0;
		for(int i = 0; i < bytes.length; i++){
			hexBytes[cursor] = HEX_DIGITS[(0xF0 & bytes[i]) >>> 4];
			++cursor;
			hexBytes[cursor] = HEX_DIGITS[0x0F & bytes[i]];
			++cursor;
		}
		return hexBytes;
	}

}

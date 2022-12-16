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

import java.util.Objects;

public class CsvIntByteStringCodec implements ByteStringCodec{

	public static final CsvIntByteStringCodec INSTANCE = new CsvIntByteStringCodec();

	@Override
	public String encode(byte[] bytes){
		Objects.requireNonNull(bytes);
		var sb = new StringBuilder();
		for(int i = 0; i < bytes.length; ++i){
			if(i > 0){
				sb.append(",");
			}
			sb.append(Byte.toUnsignedInt(bytes[i]));
		}
		return sb.toString();
	}

	@Override
	public byte[] decode(String csv){
		Objects.requireNonNull(csv);
		String[] tokens = csv.split(",");
		byte[] bytes = new byte[tokens.length];
		for(int i = 0; i < tokens.length; ++i){
			int intValue = Integer.valueOf(tokens[i]);
			if(intValue < 0 || intValue > 255){
				String message = String.format("Invalid value=%s must be between 0 and 255", intValue);
				throw new IllegalArgumentException(message);
			}
			bytes[i] = (byte)intValue;
		}
		return bytes;
	}

}

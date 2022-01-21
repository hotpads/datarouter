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
package io.datarouter.bytes.codec.booleancodec;

public class NullableBooleanCodec{

	public static final NullableBooleanCodec INSTANCE = new NullableBooleanCodec();

	private static final int NULL = -2;

	private static final RawBooleanCodec RAW_BOOLEAN_CODEC = RawBooleanCodec.INSTANCE;
	private static final int LENGTH = RAW_BOOLEAN_CODEC.length();

	public final int length(){
		return LENGTH;
	}

	public byte[] encode(Boolean value){
		if(value == null){
			return new byte[]{NULL};
		}
		return RAW_BOOLEAN_CODEC.encode(value);
	}

	public boolean decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public Boolean decode(byte[] bytes, int offset){
		if(bytes[offset] == NULL){
			return null;
		}
		return RAW_BOOLEAN_CODEC.decode(bytes, offset);
	}

}

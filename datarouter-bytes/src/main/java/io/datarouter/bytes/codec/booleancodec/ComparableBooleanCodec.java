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

public class ComparableBooleanCodec{

	public static final ComparableBooleanCodec INSTANCE = new ComparableBooleanCodec();

	private static final int LENGTH = 1;

	// Java's Boolean.compare will consider TRUE to be after FALSE
	private static final byte FALSE = 0;
	private static final byte TRUE = 1;

	public final int length(){
		return LENGTH;
	}

	public byte[] encode(boolean value){
		var bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(boolean value, byte[] bytes, int offset){
		bytes[offset] = value ? TRUE : FALSE;
		return LENGTH;
	}

	public boolean decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public boolean decode(byte[] bytes, int offset){
		byte encoded = bytes[offset];
		if(encoded == FALSE){
			return false;
		}else if(encoded == TRUE){
			return true;
		}else{
			throw new IllegalArgumentException("must be 0 or 1");
		}
	}

}

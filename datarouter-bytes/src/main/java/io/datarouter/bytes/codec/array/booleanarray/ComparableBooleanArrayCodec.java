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
package io.datarouter.bytes.codec.array.booleanarray;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.booleancodec.ComparableBooleanCodec;

public class ComparableBooleanArrayCodec{

	public static final ComparableBooleanArrayCodec INSTANCE = new ComparableBooleanArrayCodec();

	private static final ComparableBooleanCodec COMPARABLE_BOOLEAN_CODEC = ComparableBooleanCodec.INSTANCE;
	private static final int ITEM_LENGTH = COMPARABLE_BOOLEAN_CODEC.length();

	public int itemLength(){
		return ITEM_LENGTH;
	}

	public byte[] encode(boolean[] values){
		if(values.length == 0){
			return EmptyArray.BYTE;
		}
		var bytes = new byte[ITEM_LENGTH * values.length];
		encode(values, bytes, 0);
		return bytes;
	}

	public int encode(boolean[] values, byte[] bytes, int offset){
		int cursor = offset;
		for(int i = 0; i < values.length; ++i){
			COMPARABLE_BOOLEAN_CODEC.encode(values[i], bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return values.length * ITEM_LENGTH;
	}

	public boolean[] decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	public boolean[] decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return EmptyArray.BOOLEAN;
		}
		if(bytesLength % ITEM_LENGTH != 0){
			throw new IllegalArgumentException("bytesLength must be multiple of " + ITEM_LENGTH);
		}
		int resultLength = bytesLength / ITEM_LENGTH;
		var result = new boolean[resultLength];
		int cursor = offset;
		for(int i = 0; i < resultLength; ++i){
			result[i] = COMPARABLE_BOOLEAN_CODEC.decode(bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return result;
	}

}

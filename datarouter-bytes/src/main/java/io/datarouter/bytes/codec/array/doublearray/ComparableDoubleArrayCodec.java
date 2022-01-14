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
package io.datarouter.bytes.codec.array.doublearray;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.doublecodec.ComparableDoubleCodec;

public class ComparableDoubleArrayCodec{

	public static final ComparableDoubleArrayCodec INSTANCE = new ComparableDoubleArrayCodec();

	private static final ComparableDoubleCodec COMPARABLE_DOUBLE_CODEC = ComparableDoubleCodec.INSTANCE;
	private static final int ITEM_LENGTH = COMPARABLE_DOUBLE_CODEC.length();

	public int itemLength(){
		return ITEM_LENGTH;
	}

	public byte[] encode(double[] values){
		if(values.length == 0){
			return EmptyArray.BYTE;
		}
		byte[] bytes = new byte[ITEM_LENGTH * values.length];
		encode(values, bytes, 0);
		return bytes;
	}

	public int encode(double[] values, byte[] bytes, int offset){
		int cursor = offset;
		for(int i = 0; i < values.length; ++i){
			COMPARABLE_DOUBLE_CODEC.encode(values[i], bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return values.length * ITEM_LENGTH;
	}

	public double[] decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	public double[] decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return EmptyArray.DOUBLE;
		}
		if(bytesLength % ITEM_LENGTH != 0){
			throw new IllegalArgumentException("bytesLength must be multiple of " + ITEM_LENGTH);
		}
		int resultLength = bytesLength / ITEM_LENGTH;
		double[] result = new double[resultLength];
		int cursor = offset;
		for(int i = 0; i < resultLength; ++i){
			result[i] = COMPARABLE_DOUBLE_CODEC.decode(bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return result;
	}

}

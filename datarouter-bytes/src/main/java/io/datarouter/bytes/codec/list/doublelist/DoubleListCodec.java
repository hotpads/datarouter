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
package io.datarouter.bytes.codec.list.doublelist;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.doublecodec.RawDoubleCodec;

public class DoubleListCodec{

	public static final DoubleListCodec INSTANCE = new DoubleListCodec();

	private static final RawDoubleCodec RAW_DOUBLE_CODEC = RawDoubleCodec.INSTANCE;
	private static final int ITEM_LENGTH = RAW_DOUBLE_CODEC.length();

	public byte[] encode(List<Double> values){
		if(values.isEmpty()){
			return EmptyArray.BYTE;
		}
		var bytes = new byte[ITEM_LENGTH * values.size()];
		int cursor = 0;
		for(int i = 0; i < values.size(); ++i){
			RAW_DOUBLE_CODEC.encode(values.get(i), bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return bytes;
	}

	public List<Double> decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	public List<Double> decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return new ArrayList<>(0);
		}
		if(bytesLength % ITEM_LENGTH != 0){
			throw new IllegalArgumentException("bytesLength must be multiple of " + ITEM_LENGTH);
		}
		int numValues = (bytes.length - offset) / ITEM_LENGTH;
		List<Double> values = new ArrayList<>(numValues);
		int cursor = offset;
		for(int i = 0; i < numValues; i++){
			double value = RAW_DOUBLE_CODEC.decode(bytes, cursor);
			cursor += ITEM_LENGTH;
			values.add(value);
		}
		return values;
	}

}

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
package io.datarouter.bytes.codec.list.intlist;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;

public class IntListCodec{

	public static final IntListCodec INSTANCE = new IntListCodec();
	public static final Codec<List<Integer>,byte[]> CODEC = Codec.of(INSTANCE::encode, INSTANCE::decode);

	private static final ComparableIntCodec COMPARABLE_INT_CODEC = ComparableIntCodec.INSTANCE;
	private static final int ITEM_LENGTH = COMPARABLE_INT_CODEC.length();

	public byte[] encode(List<Integer> values){
		if(values.isEmpty()){
			return EmptyArray.BYTE;
		}
		var bytes = new byte[ITEM_LENGTH * values.size()];
		encode(values, bytes, 0);
		return bytes;
	}

	public int encode(List<Integer> values, byte[] bytes, int offset){
		int cursor = offset;
		for(int i = 0; i < values.size(); ++i){
			COMPARABLE_INT_CODEC.encode(values.get(i), bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return values.size() * ITEM_LENGTH;
	}

	public List<Integer> decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	public List<Integer> decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return new ArrayList<>(0);
		}
		if(bytesLength % ITEM_LENGTH != 0){
			throw new IllegalArgumentException("bytesLength must be multiple of " + ITEM_LENGTH);
		}
		int resultLength = bytesLength / ITEM_LENGTH;
		List<Integer> result = new ArrayList<>(resultLength);
		int cursor = offset;
		for(int i = 0; i < resultLength; ++i){
			int value = COMPARABLE_INT_CODEC.decode(bytes, cursor);
			cursor += ITEM_LENGTH;
			result.add(value);
		}
		return result;
	}

}

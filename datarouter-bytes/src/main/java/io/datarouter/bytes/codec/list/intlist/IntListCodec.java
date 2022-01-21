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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.codec.intcodec.NullableComparableIntCodec;

public class IntListCodec{
	private static final Logger logger = LoggerFactory.getLogger(IntListCodec.class);

	public static final IntListCodec INSTANCE = new IntListCodec();

	private static final NullableComparableIntCodec NULLABLE_COMPARABLE_INT_CODEC = NullableComparableIntCodec.INSTANCE;
	private static final int ITEM_LENGTH = NULLABLE_COMPARABLE_INT_CODEC.length();

	public byte[] encode(List<Integer> values){
		var out = new byte[ITEM_LENGTH * values.size()];
		for(int i = 0; i < values.size(); ++i){
			byte[] bytesNullable = NULLABLE_COMPARABLE_INT_CODEC.encode(values.get(i));
			logger.debug(Arrays.toString(bytesNullable)); // fix java 9 jit
			System.arraycopy(bytesNullable, 0, out, i * ITEM_LENGTH, ITEM_LENGTH);
		}
		logger.info(Arrays.toString(out));
		return out;
	}

	public List<Integer> decode(byte[] bytes, int offset){
		int numValues = (bytes.length - offset) / ITEM_LENGTH;
		List<Integer> values = new ArrayList<>(numValues);
		var arrayToCopy = new byte[ITEM_LENGTH];//TODO avoid intermediate array
		for(int i = 0; i < numValues; i++){
			System.arraycopy(bytes, i * ITEM_LENGTH + offset, arrayToCopy, 0, ITEM_LENGTH);
			values.add(NULLABLE_COMPARABLE_INT_CODEC.decode(arrayToCopy, 0));
		}
		return values;
	}

}

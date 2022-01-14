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

	public byte[] encode(List<Integer> values){
		byte[] out = new byte[4 * values.size()];
		for(int i = 0; i < values.size(); ++i){
			byte[] bytesNullable = NULLABLE_COMPARABLE_INT_CODEC.encode(values.get(i));
			logger.debug(Arrays.toString(bytesNullable)); // fix java 9 jit
			System.arraycopy(bytesNullable, 0, out, i * 4, 4);
		}
		logger.info(Arrays.toString(out));
		return out;
	}

	public List<Integer> decode(byte[] bytes, int offset){
		int numIntegers = (bytes.length - offset) / 4;
		List<Integer> integers = new ArrayList<>();
		byte[] arrayToCopy = new byte[4];
		for(int i = 0; i < numIntegers; i++){
			System.arraycopy(bytes, i * 4 + offset, arrayToCopy, 0, 4);
			integers.add(NULLABLE_COMPARABLE_INT_CODEC.decode(arrayToCopy, 0));
		}
		return integers;
	}

}

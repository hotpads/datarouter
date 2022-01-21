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
package io.datarouter.bytes.codec.list.booleanlist;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.codec.booleancodec.NullableBooleanCodec;

public class BooleanListCodec{

	public static final BooleanListCodec INSTANCE = new BooleanListCodec();

	private static final NullableBooleanCodec NULLABLE_BOOLEAN_CODEC = NullableBooleanCodec.INSTANCE;
	private static final int ITEM_LENGTH = NULLABLE_BOOLEAN_CODEC.length();

	public byte[] encode(List<Boolean> values){
		var out = new byte[values.size()];
		for(int i = 0; i < values.size(); i++){
			System.arraycopy(NULLABLE_BOOLEAN_CODEC.encode(values.get(i)), 0, out, i, ITEM_LENGTH);
		}
		return out;
	}

	public List<Boolean> decode(byte[] bytes, int offset){
		int numValues = bytes.length - offset;
		List<Boolean> values = new ArrayList<>(numValues);
		var arrayToCopy = new byte[ITEM_LENGTH];//TODO avoid intermediate array
		for(int i = 0; i < numValues; i++){
			System.arraycopy(bytes, i + offset, arrayToCopy, 0, ITEM_LENGTH);
			values.add(NULLABLE_BOOLEAN_CODEC.decode(arrayToCopy, 0));
		}
		return values;
	}

}

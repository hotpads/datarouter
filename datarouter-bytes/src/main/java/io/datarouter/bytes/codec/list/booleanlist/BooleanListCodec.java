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

	public byte[] encode(List<Boolean> values){
		byte[] out = new byte[values.size()];
		for(int i = 0; i < values.size(); i++){
			System.arraycopy(NULLABLE_BOOLEAN_CODEC.encode(values.get(i)), 0, out, i, 1);
		}
		return out;
	}

	public List<Boolean> decode(byte[] bytes, int offset){
		int numBooleans = bytes.length - offset;
		List<Boolean> bools = new ArrayList<>();
		byte[] arrayToCopy = new byte[1];
		for(int i = 0; i < numBooleans; i++){
			System.arraycopy(bytes, i + offset, arrayToCopy, 0, 1);
			bools.add(NULLABLE_BOOLEAN_CODEC.decode(arrayToCopy, 0));
		}
		return bools;
	}

}

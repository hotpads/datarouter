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
package io.datarouter.model.field.imp.list;

import java.util.List;

import io.datarouter.bytes.codec.list.intlist.IntListCodec;
import io.datarouter.model.field.BaseListField;

public class IntListField extends BaseListField<Integer,List<Integer>,IntListFieldKey>{

	private static final IntListCodec INT_LIST_CODEC = IntListCodec.INSTANCE;

	public IntListField(IntListFieldKey key, List<Integer> value){
		super(key, value);
	}

	@Override
	public List<Integer> parseStringEncodedValueButDoNotSet(String value){
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : INT_LIST_CODEC.encode(value);
	}

	@Override
	public List<Integer> fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return INT_LIST_CODEC.decode(bytes, offset, length);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();
	}

}

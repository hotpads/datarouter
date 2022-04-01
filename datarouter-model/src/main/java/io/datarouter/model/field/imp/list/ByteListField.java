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

import java.util.Arrays;
import java.util.List;

import io.datarouter.bytes.ByteTool;
import io.datarouter.model.field.BaseListField;
import io.datarouter.model.field.Field;

@Deprecated//Use ByteArrayField
public class ByteListField extends BaseListField<Byte,List<Byte>,ByteListFieldKey>{

	public ByteListField(ByteListFieldKey key, List<Byte> value){
		super(key, value);
	}

	@Override
	public List<Byte> parseStringEncodedValueButDoNotSet(String value){
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getBytes(){
		throw new UnsupportedOperationException();
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Byte> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Byte> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(Field<List<Byte>> other){
		//not the most efficient to allocate a byte[], but it's deprecated
		byte[] thisArray = ByteTool.fromBoxedBytes(value);
		byte[] otherArray = ByteTool.fromBoxedBytes(value);
		return Arrays.compareUnsigned(thisArray, otherArray);
	}

}

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
package io.datarouter.model.field.imp.comparable;

import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.string.StringTool;

public class IntegerField extends BasePrimitiveField<Integer,IntegerFieldKey>{

	private static final ComparableIntCodec COMPARABLE_INT_CODEC = ComparableIntCodec.INSTANCE;

	public IntegerField(IntegerFieldKey key, Integer value){
		this(null, key, value);
	}

	public IntegerField(String prefix, IntegerFieldKey key, Integer value){
		super(prefix, key, value);
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Integer parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Integer.valueOf(str);
	}

	@Override
	public byte[] getValueBytes(){
		return value == null ? null : COMPARABLE_INT_CODEC.encode(value);
	}

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}

	@Override
	public Integer fromValueBytesButDoNotSet(byte[] bytes, int offset){
		return COMPARABLE_INT_CODEC.decode(bytes, offset);
	}

}

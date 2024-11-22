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

import io.datarouter.bytes.codec.doublecodec.ComparableDoubleCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.string.StringTool;

public class DoubleField extends BasePrimitiveField<Double,DoubleFieldKey>{

	private static final ComparableDoubleCodec COMPARABLE_CODEC = ComparableDoubleCodec.INSTANCE;

	public DoubleField(DoubleFieldKey key, Double value){
		super(key, value);
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Double parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Double.valueOf(str);
	}

	@Override
	public byte[] getValueBytes(){
		return value == null ? null : COMPARABLE_CODEC.encode(value);
	}

	@Override
	public int getApproximateValueBytesLength(){
		return value == null ? 0 : COMPARABLE_CODEC.length();
	}

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		return COMPARABLE_CODEC.length();
	}

	@Override
	public Double fromValueBytesButDoNotSet(byte[] bytes, int offset){
		return COMPARABLE_CODEC.decode(bytes, offset);
	}

}

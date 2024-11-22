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

import io.datarouter.bytes.codec.booleancodec.RawBooleanCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.string.StringTool;

public class BooleanField extends BasePrimitiveField<Boolean,BooleanFieldKey>{

	private static final RawBooleanCodec RAW_BOOLEAN_CODEC = RawBooleanCodec.INSTANCE;

	public BooleanField(BooleanFieldKey key, Boolean value){
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
	public Boolean parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return BooleanTool.isTrue(str);
	}

	@Override
	public byte[] getValueBytes(){
		return value == null ? null : RAW_BOOLEAN_CODEC.encode(value);
	}

	@Override
	public int getApproximateValueBytesLength(){
		return value == null ? 0 : RAW_BOOLEAN_CODEC.length();
	}

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		return 1;
	}

	@Override
	public Boolean fromValueBytesButDoNotSet(byte[] bytes, int offset){
		return RAW_BOOLEAN_CODEC.decode(bytes, offset);
	}

}

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

import io.datarouter.bytes.codec.bytecodec.ComparableByteCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.string.StringTool;

//recognizes -128 to -1 using two's complement.  therefore max value is 127
public class SignedByteField extends BasePrimitiveField<Byte,SignedByteFieldKey>{

	private static final ComparableByteCodec COMPARABLE_BYTE_CODEC = ComparableByteCodec.INSTANCE;

	public SignedByteField(SignedByteFieldKey key, Byte value){
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
	public Byte parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Byte.valueOf(str);
	}

	//recognizes -128 to -1 using two's complement.  therefore max value is 127
	@Override
	public byte[] getBytes(){
		return value == null ? null : COMPARABLE_BYTE_CODEC.encode(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 1;
	}

	@Override
	public Byte fromBytesButDoNotSet(byte[] bytes, int offset){
		return COMPARABLE_BYTE_CODEC.decode(bytes, offset);
	}

}

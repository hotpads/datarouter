/**
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
package io.datarouter.model.field.imp.positive;

import java.util.Random;

import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.bytes.LongByteTool;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.string.StringTool;

public class UInt63Field extends BasePrimitiveField<Long>{

	private static final Random random = new Random();

	public UInt63Field(String prefix, UInt63FieldKey key, Long value){
		super(prefix, key, value);
	}

	public UInt63Field(UInt63FieldKey key, Long value){
		this(null, key, value);
	}

	public static long nextPositiveRandom(){
		return RandomTool.nextPositiveLong(random);
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Long parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Long.valueOf(str);
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : LongByteTool.getUInt63Bytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public Long fromBytesButDoNotSet(byte[] bytes, int offset){
		return LongByteTool.fromUInt63Bytes(bytes, offset);
	}

}

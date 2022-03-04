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
package io.datarouter.model.field.imp.custom;

import java.util.Date;

import io.datarouter.bytes.codec.longcodec.RawLongCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.string.StringTool;

public class LongDateField extends BasePrimitiveField<Date,LongDateFieldKey>{

	private static final RawLongCodec RAW_LONG_CODEC = RawLongCodec.INSTANCE;

	public LongDateField(LongDateFieldKey key, Date value){
		super(key, value);
	}

	/**
	 * @return the "long" format for persistence.  Note that getValueString() returns the human-readable date
	 */
	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.getTime() + "";
	}

	@Override
	public Date parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return new Date(Long.valueOf(str));
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return encodeToBytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return RAW_LONG_CODEC.length();
	}

	@Override
	public Date fromBytesButDoNotSet(byte[] bytes, int offset){
		return decodeFromBytes(bytes, offset);
	}

	public static byte[] encodeToBytes(Date value){
		long time = value.getTime();
		return RAW_LONG_CODEC.encode(time);
	}

	public static Date decodeFromBytes(byte[] bytes, int offset){
		long time = RAW_LONG_CODEC.decode(bytes, offset);
		return new Date(time);
	}

}

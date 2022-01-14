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
package io.datarouter.model.field.imp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.datarouter.bytes.codec.shortcodec.ComparableShortCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.string.StringTool;

public class LocalDateField extends BasePrimitiveField<LocalDate,LocalDateFieldKey>{

	private static final ComparableShortCodec COMPARABLE_SHORT_CODEC = ComparableShortCodec.INSTANCE;
	private static final int NUM_BYTES = 4;

	public LocalDateField(LocalDateFieldKey key, LocalDate value){
		this(null, key, value);
	}

	public LocalDateField(String prefix, LocalDateFieldKey key, LocalDate value){
		super(prefix, key, value);
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	@Override
	public LocalDate parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isNullOrEmpty(str)){
			return null;
		}
		return LocalDate.parse(str, DateTimeFormatter.ISO_LOCAL_DATE);
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		byte[] bytes = new byte[NUM_BYTES];
		//limited to year 32,767 (16 bits signed)
		COMPARABLE_SHORT_CODEC.encode((short)value.getYear(), bytes, 0);
		bytes[2] = (byte)value.getMonthValue();//at most 5 bits signed
		bytes[3] = (byte)value.getDayOfMonth();//at most 6 bits signed
		return bytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return NUM_BYTES;
	}

	@Override
	public LocalDate fromBytesButDoNotSet(byte[] bytes, int offset){
		int year = COMPARABLE_SHORT_CODEC.decode(bytes, offset);
		int month = bytes[offset + 2];
		int day = bytes[offset + 3];
		return LocalDate.of(year, month, day);
	}

}

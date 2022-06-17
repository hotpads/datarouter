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
package io.datarouter.model.field.imp.enums;

import java.util.Comparator;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.enums.StringEnum;
import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;

public class StringEnumField<E extends StringEnum<E>> extends BaseField<E>{

	private static final Comparator<StringEnum<?>> VALUE_COMPARATOR = Comparator.nullsFirst(
			Comparator.comparing(StringEnum::getPersistentString));

	private final StringEnumFieldKey<E> key;
	private final StringField stringField;

	public StringEnumField(StringEnumFieldKey<E> key, E value){
		this(key, value, null);
	}

	public StringEnumField(StringEnumFieldKey<E> key, E value, String prefix){
		super(prefix, value);
		this.key = key;
		this.stringField = toStringField(this);
	}

	@Override
	public StringEnumFieldKey<E> getKey(){
		return key;
	}

	@Override
	public int compareTo(Field<E> other){
		return VALUE_COMPARATOR.compare(value, other.getValue());
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.getPersistentString();
	}

	@Override
	public E parseStringEncodedValueButDoNotSet(String string){
		return StringEnum.fromPersistentStringSafe(getSampleValue(), string);
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : StringCodec.UTF_8.encode(value.getPersistentString());
	}

	@Override
	public byte[] getBytesWithSeparator(){
		return stringField.getBytesWithSeparator();
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return stringField.numBytesWithSeparator(bytes, offset);
	}

	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		String stringValue = stringField.fromBytesButDoNotSet(bytes, offset);
		return StringEnum.fromPersistentStringSafe(getSampleValue(), stringValue);
	}

	@Override
	public E fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		String stringValue = stringField.fromBytesWithSeparatorButDoNotSet(bytes, offset);
		return StringEnum.fromPersistentStringSafe(getSampleValue(), stringValue);
	}

	@Override
	public String getValueString(){
		return value == null ? "null" : value.getPersistentString();
	}

	public E getSampleValue(){
		return key.getSampleValue();
	}

	public static StringField toStringField(StringEnumField<?> stringEnumField){
		if(stringEnumField == null){
			return null;
		}
		String value = null;
		if(stringEnumField.getValue() != null){
			value = stringEnumField.getValue().getPersistentString();
		}
		String defaultValue = null;
		if(stringEnumField.getKey().getDefaultValue() != null){
			defaultValue = stringEnumField.getKey().getDefaultValue().getPersistentString();
		}

		StringFieldKey key = new StringFieldKey(
				stringEnumField.key.getName(),
				stringEnumField.key.getColumnName(),
				stringEnumField.key.isNullable(),
				FieldGeneratorType.NONE, defaultValue,
				stringEnumField.key.getSize(),
				true,
				stringEnumField.key.getAttributes());

		return new StringField(stringEnumField.getPrefix(), key, value);
	}

}

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
package io.datarouter.model.field.imp;

import java.util.Map;

import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.util.CommonFieldSizes;

public class StringFieldKey extends BaseFieldKey<String,StringFieldKey>{

	private static final int DEFAULT_MAX_SIZE = CommonFieldSizes.DEFAULT_LENGTH_VARCHAR;

	private final int size;

	public StringFieldKey(String name){
		super(name, String.class);
		this.size = DEFAULT_MAX_SIZE;
	}

	public StringFieldKey(
			String name,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			String defaultValue,
			int size,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, String.class, fieldGeneratorType, defaultValue, attributes);
		this.size = size;
	}

	public StringFieldKey withSize(int sizeOverride){
		return new StringFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, sizeOverride,
				attributes);
	}

	public StringFieldKey withColumnName(String columnNameOverride){
		return new StringFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue, size,
				attributes);
	}

	public StringFieldKey notNullable(){
		return new StringFieldKey(name, columnName, false, fieldGeneratorType, defaultValue, size, attributes);
	}

	@Override
	public boolean isFixedLength(){
		return false;
	}

	public int getSize(){
		return size;
	}

	@Override
	public StringField createValueField(String value){
		return new StringField(this, value);
	}

}

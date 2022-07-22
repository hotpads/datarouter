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

import java.util.Map;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;

import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.util.CommonFieldSizes;

public class StringFieldKey extends BaseFieldKey<String,StringFieldKey>{

	private static final int DEFAULT_MAX_SIZE = CommonFieldSizes.DEFAULT_LENGTH_VARCHAR;

	private final int size;
	private final boolean logInvalidSize;
	private final boolean validateSize;

	public StringFieldKey(String name){
		super(name, TypeToken.get(String.class));
		this.size = DEFAULT_MAX_SIZE;
		this.logInvalidSize = true;
		this.validateSize = true;
	}

	public StringFieldKey(
			String name,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			String defaultValue,
			int size,
			boolean logInvalidSize,
			boolean validateSize,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, TypeToken.get(String.class), fieldGeneratorType, defaultValue, attributes);
		this.size = size;
		this.logInvalidSize = logInvalidSize;
		this.validateSize = validateSize;
	}

	public StringFieldKey withSize(int sizeOverride){
		return new StringFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, sizeOverride,
				logInvalidSize, validateSize, attributes);
	}

	public StringFieldKey withColumnName(String columnNameOverride){
		return new StringFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue, size,
				logInvalidSize, validateSize, attributes);
	}

	public StringFieldKey notNullable(){
		return new StringFieldKey(name, columnName, false, fieldGeneratorType, defaultValue, size, logInvalidSize,
				validateSize, attributes);
	}

	/**
	 * @deprecated Increase the size using .withSize(..) or restrict values to the current size.
	 */
	@Deprecated
	public StringFieldKey disableInvalidSizeLogging(){
		return new StringFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, size, false,
				validateSize, attributes);
	}

	/**
	 * @deprecated Increase the size using .withSize(..) or restrict values to the current size.
	 */
	@Deprecated
	public StringFieldKey disableSizeValidation(){
		return new StringFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, size, logInvalidSize,
				false, attributes);
	}

	@Override
	public boolean isFixedLength(){
		return false;
	}

	@Override
	public boolean isPossiblyCaseInsensitive(){
		return true;
	}

	@Override
	public Optional<Integer> findSize(){
		return Optional.of(size);
	}

	public int getSize(){
		return size;
	}

	public boolean shouldLogInvalidSize(){
		return logInvalidSize;
	}

	public boolean shouldValidateSize(){
		return validateSize;
	}

	@Override
	public String getSampleValue(){
		return "";
	}

}

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

import java.time.LocalDate;

import io.datarouter.model.field.PrimitiveFieldKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;

public class LocalDateFieldKey extends PrimitiveFieldKey<LocalDate>{

	public LocalDateFieldKey(String name){
		super(name, LocalDate.class);
	}

	private LocalDateFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			LocalDate defaultValue){
		super(name, columnName, nullable, LocalDate.class, fieldGeneratorType, defaultValue);
	}

	public LocalDateFieldKey withColumnName(String columnNameOverride){
		return new LocalDateFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}

	@Override
	public LocalDateField createValueField(final LocalDate value){
		return new LocalDateField(this, value);
	}

}

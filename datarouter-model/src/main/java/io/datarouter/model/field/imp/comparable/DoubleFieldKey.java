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
package io.datarouter.model.field.imp.comparable;

import io.datarouter.model.field.PrimitiveFieldKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;

public class DoubleFieldKey extends PrimitiveFieldKey<Double>{

	public DoubleFieldKey(String name){
		super(name, Double.class);
	}

	private DoubleFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Double defaultValue){
		super(name, columnName, nullable, Double.class, fieldGeneratorType, defaultValue);
	}

	public DoubleFieldKey withColumnName(String columnNameOverride){
		return new DoubleFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}

	@Override
	public DoubleField createValueField(final Double value){
		return new DoubleField(this, value);
	}

}

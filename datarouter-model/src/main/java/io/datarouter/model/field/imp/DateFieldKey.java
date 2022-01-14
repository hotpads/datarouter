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

import java.util.Date;
import java.util.Map;

import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.PrimitiveFieldKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;

/**
 * @deprecated use InstantFieldKey or LongFieldKey instead
 */
@Deprecated
public class DateFieldKey extends PrimitiveFieldKey<Date,DateFieldKey>{

	private static final int DEFAULT_DECIMAL_SECONDS = 3;// match java's millisecond precision

	private final int numDecimalSeconds;

	/**
	 * Defines a DateFieldKey with seconds precision
	 *
	 * New usages should try to use Instants, LocalDate, LocalDateTime, or Longs. Daylight savings and database timezone
	 * migrations result in broken date field parsing.
	 *
	 * @deprecated use {@link io.datarouter.model.field.imp.comparable.InstantFieldKey},
	 *             {@link io.datarouter.model.field.imp.LocalDateFieldKey},
	 *             {@link io.datarouter.model.field.imp.custom.LocalDateTimeFieldKey},
	 *             {@link io.datarouter.model.field.imp.comparable.LongFieldKey}
	 */
	@Deprecated
	public DateFieldKey(String name){
		super(name, Date.class);
		this.numDecimalSeconds = DEFAULT_DECIMAL_SECONDS;
	}

	private DateFieldKey(
			String name,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			Date defaultValue,
			int numDecimalSeconds,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, Date.class, fieldGeneratorType, defaultValue, attributes);
		this.numDecimalSeconds = numDecimalSeconds;
	}

	public DateFieldKey withColumnName(String columnNameOverride){
		return new DateFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue,
				numDecimalSeconds, attributes);
	}

	public int getNumDecimalSeconds(){
		return numDecimalSeconds;
	}

	public DateFieldKey withSecondsPrecision(){
		return withPrecision(0);
	}

	public DateFieldKey withPrecision(int precision){
		return new DateFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, precision, attributes);
	}

	@Override
	public DateField createValueField(Date value){
		return new DateField(this, value);
	}

}

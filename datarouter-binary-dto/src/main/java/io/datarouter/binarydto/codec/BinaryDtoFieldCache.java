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
package io.datarouter.binarydto.codec;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.binarydto.internal.BinaryDtoAllocator;
import io.datarouter.binarydto.internal.BinaryDtoFieldSchema;
import io.datarouter.binarydto.internal.BinaryDtoMetadataParser;
import io.datarouter.scanner.Scanner;

public class BinaryDtoFieldCache<T extends BaseBinaryDto<T>>{

	public final Class<T> dtoClass;
	public final List<Field> fieldByIndex;//may contain nulls for missing indexes
	public final List<Field> presentFields;
	public final List<? extends BinaryDtoFieldSchema<?>> fieldSchemaByIndex;
	public final List<? extends BinaryDtoFieldSchema<?>> presentFieldSchemas;

	public BinaryDtoFieldCache(Class<T> dtoClass){
		this.dtoClass = dtoClass;
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		fieldByIndex = new BinaryDtoMetadataParser<>(dto).listFields();
		presentFields = Scanner.of(fieldByIndex)
				.exclude(Objects::isNull)
				.list();
		fieldSchemaByIndex = Scanner.of(fieldByIndex)
				.map(field -> {
					if(field == null){
						return null;
					}
					field.setAccessible(true);
					BinaryDtoFieldSchema<?> fieldSchema = new BinaryDtoFieldSchema<>(field);
					return fieldSchema;
				})
				.list();
		presentFieldSchemas = Scanner.of(fieldSchemaByIndex)
				.exclude(Objects::isNull)
				.list();
	}

}

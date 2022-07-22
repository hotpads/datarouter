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
package io.datarouter.binarydto.internal;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.scanner.Scanner;

public class BinaryDtoMetadataParser<T extends BaseBinaryDto<T>>{

	private final T dto;

	public BinaryDtoMetadataParser(T dto){
		this.dto = dto;
	}

	public List<Field> listFields(){
		return anyIndexSpecified()
				? listFieldsByIndex()
				: listFieldsAlphabetically();
	}

	private List<Field> listFieldsByIndex(){
		Field[] fields = new Field[maxFieldIndex() + 1];
		scanFields()
				.map(BinaryDtoFieldMetadataParser::new)
				.forEach(metadata -> {
					int index = metadata.getRequiredIndex();
					if(index < 0){
						String message = String.format("index=%s cannot be negative", index);
						throw new IllegalArgumentException(message);
					}
					if(fields[index] != null){
						String message = String.format("index=%s already specified", index);
						throw new IllegalArgumentException(message);
					}
					fields[index] = metadata.getField();
				});
		return Arrays.asList(fields);
	}

	private List<Field> listFieldsAlphabetically(){
		return scanFields()
				.each(field -> {
					if(new BinaryDtoFieldMetadataParser<>(field).hasIndex()){
						String message = String.format(
								"Classes extending %s cannot have BinaryDtoField index specified.  Field=%s",
								getClass().getCanonicalName(),
								field.getName());
						throw new IllegalArgumentException(message);
					}
				})
				.sort(Comparator.comparing(Field::getName))
				.list();
	}

	private Scanner<Field> scanFields(){
		return BinaryDtoReflectionTool.scanFieldsIncludingSuperclasses(dto.getClass());
	}

	private boolean anyIndexSpecified(){
		return scanFields()
				.map(BinaryDtoFieldMetadataParser::new)
				.anyMatch(BinaryDtoFieldMetadataParser::hasIndex);
	}

	private int maxFieldIndex(){
		return scanFields()
				.map(BinaryDtoFieldMetadataParser::new)
				.map(BinaryDtoFieldMetadataParser::getRequiredIndex)
				.findMax(Comparator.naturalOrder())
				.orElseThrow();
	}

}

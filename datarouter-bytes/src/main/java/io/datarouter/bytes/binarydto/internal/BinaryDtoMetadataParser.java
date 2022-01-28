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
package io.datarouter.bytes.binarydto.internal;

import java.lang.reflect.Field;
import java.util.Comparator;

import io.datarouter.bytes.binarydto.dto.BinaryDto;
import io.datarouter.scanner.Scanner;

public class BinaryDtoMetadataParser<T extends BinaryDto<T>>{

	private final T dto;

	public BinaryDtoMetadataParser(T dto){
		this.dto = dto;
	}

	public int numFields(){
		return (int)BinaryDtoReflectionTool.scanFieldsIncludingSuperclasses(dto.getClass())
				.count();
	}

	public Scanner<Field> scanFieldsOrdered(){
		return anyIndexSpecified()
				? scanFieldsInAnnotationOrder()
				: scanFieldsInAlphabeticalOrder();
	}

	private Scanner<Field> scanFieldsInAnnotationOrder(){
		int numFields = numFields();
		int maxIndex = numFields - 1;
		Field[] orderedFields = new Field[numFields];
		BinaryDtoReflectionTool.scanFieldsIncludingSuperclasses(dto.getClass())
				.map(BinaryDtoFieldMetadataParser::new)
				.forEach(metadata -> {
					int index = metadata.getRequiredIndex();
					if(index < 0){
						String message = String.format("index=%s cannot be negative", index);
						throw new IllegalArgumentException(message);
					}
					if(index > maxIndex){
						String message = String.format("index=%s is greater than maxIndex=%s", index, maxIndex);
						throw new IllegalArgumentException(message);
					}
					if(orderedFields[index] != null){
						String message = String.format("index=%s already specified", index);
						throw new IllegalArgumentException(message);
					}
					orderedFields[index] = metadata.getField();
				});
		return Scanner.of(orderedFields);
	}

	private Scanner<Field> scanFieldsInAlphabeticalOrder(){
		return BinaryDtoReflectionTool.scanFieldsIncludingSuperclasses(dto.getClass())
				.each(field -> {
					if(new BinaryDtoFieldMetadataParser<>(field).hasIndex()){
						String message = String.format(
								"Classes extending %s cannot have BinaryDtoField index specified.  Field=%s",
								getClass().getCanonicalName(),
								field.getName());
						throw new IllegalArgumentException(message);
					}
				})
				.sort(Comparator.comparing(Field::getName));
	}

	private boolean anyIndexSpecified(){
		return BinaryDtoReflectionTool.scanFieldsIncludingSuperclasses(dto.getClass())
				.map(BinaryDtoFieldMetadataParser::new)
				.anyMatch(BinaryDtoFieldMetadataParser::hasIndex);
	}

}

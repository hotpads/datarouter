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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.binarydto.fieldcodec.other.ListBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.other.NestedBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.other.ObjectArrayBinaryDtoFieldCodec;

public class BinaryDtoFieldCache<T extends BaseBinaryDto<T>>{

	private static final Map<Class<? extends BaseBinaryDto<?>>,BinaryDtoFieldCache<?>> CACHE
			= new ConcurrentHashMap<>();

	// Counters for testing or debugging.
	// Should be one instantiation per class.
	private static final Map<Class<?>,AtomicLong> INVOCATION_COUNT_BY_CLASS = new ConcurrentHashMap<>();

	public final Class<T> dtoClass;
	public final List<Field> fieldByIndex;//may contain nulls for missing indexes
	public final List<Field> presentFields;
	public final List<? extends BinaryDtoFieldSchema<?>> fieldSchemaByIndex;
	public final List<? extends BinaryDtoFieldSchema<?>> presentFieldSchemas;

	private BinaryDtoFieldCache(
			Class<T> dtoClass,
			List<Field> fieldByIndex,
			List<Field> presentFields,
			List<? extends BinaryDtoFieldSchema<?>> fieldSchemaByIndex,
			List<? extends BinaryDtoFieldSchema<?>> presentFieldSchemas){
		this.dtoClass = dtoClass;
		this.fieldByIndex = fieldByIndex;
		this.presentFields = presentFields;
		this.fieldSchemaByIndex = fieldSchemaByIndex;
		this.presentFieldSchemas = presentFieldSchemas;
	}

	/*-------- static constructors --------*/

	/**
	 * Caching static constructor.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseBinaryDto<T>>
	BinaryDtoFieldCache<T> of(Class<T> dtoClass){
		BinaryDtoFieldCache<?> fieldCacheUntyped = CACHE.computeIfAbsent(
				dtoClass,
				_ -> (BinaryDtoFieldCache<T>) makeInternal(dtoClass));
		return (BinaryDtoFieldCache<T>) fieldCacheUntyped;
	}

	/**
	 * Actual static constructor.
	 */
	private static <T extends BaseBinaryDto<T>>
	BinaryDtoFieldCache<T> makeInternal(Class<T> dtoClass){
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		List<Field> fieldByIndex = new BinaryDtoMetadataParser<>(dto).listFields();
		List<Field> presentFields = new ArrayList<>();
		List<BinaryDtoFieldSchema<?>> fieldSchemaByIndex = new ArrayList<>();
		List<BinaryDtoFieldSchema<?>> presentFieldSchemas = new ArrayList<>();
		for(Field field : fieldByIndex){
			if(field == null){
				fieldSchemaByIndex.add(null);
			}else{
				presentFields.add(field);
				field.setAccessible(true);
				var fieldMetadataParser = new BinaryDtoFieldMetadataParser<>(field);
				BinaryDtoBaseFieldCodec<?> untypedFieldCodec = fieldMetadataParser.isItemTypeBinaryDto()
						? getCodecForNestedField(fieldMetadataParser)
						: BinaryDtoFieldCodecs.getCodecForLeafField(fieldMetadataParser);
				Objects.requireNonNull(untypedFieldCodec, "Codec not found for " + field);
				BinaryDtoFieldSchema<?> fieldSchema = BinaryDtoFieldSchema.create(
						fieldMetadataParser,
						untypedFieldCodec);
				fieldSchemaByIndex.add(fieldSchema);
				presentFieldSchemas.add(fieldSchema);
			}
		}
		INVOCATION_COUNT_BY_CLASS.computeIfAbsent(dtoClass, _ -> new AtomicLong()).incrementAndGet();
		return new BinaryDtoFieldCache<>(
				dtoClass,
				fieldByIndex,
				presentFields,
				fieldSchemaByIndex,
				presentFieldSchemas);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static BinaryDtoBaseFieldCodec<?> getCodecForNestedField(
			BinaryDtoFieldMetadataParser<?> fieldMetadataParser){
		Class<?> itemClass = fieldMetadataParser.getItemClass();
		var itemCodec = createNestedCodec(fieldMetadataParser.getItemClass());
		if(fieldMetadataParser.isObjectArray()){
			return new ObjectArrayBinaryDtoFieldCodec(
					itemClass,
					itemCodec,
					fieldMetadataParser.isNullableItems());
		}else if(fieldMetadataParser.isList()){
			return new ListBinaryDtoFieldCodec<>(
					itemCodec,
					fieldMetadataParser.isNullableItems());
		}else{
			return itemCodec;
		}
	}

	/**
	 * The supplier is for lazy initialization of the BinaryDtoIndexedCodec.
	 * Otherwise this field cache layer may loop forever with recursive nested fields.
	 */
	@SuppressWarnings({"unchecked"})
	private static <T extends BaseBinaryDto<T>>
	NestedBinaryDtoFieldCodec<T> createNestedCodec(Class<?> dtoClass){
		Class<T> typedDtoClass = (Class<T>)dtoClass;
		Supplier<BinaryDtoIndexedCodec<T>> indexedCodecSupplier = () -> BinaryDtoIndexedCodec.of(typedDtoClass);
		return new NestedBinaryDtoFieldCodec<>(indexedCodecSupplier);
	}

	/*-------- invocation counts --------*/

	public static <T extends BaseBinaryDto<T>>
	long invocationCountForClass(Class<T> binaryDtoClass){
		AtomicLong invocationCount = INVOCATION_COUNT_BY_CLASS.get(binaryDtoClass);
		return invocationCount == null ? 0 : invocationCount.get();
	}

}

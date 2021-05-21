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
package io.datarouter.gcp.spanner.ddl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.cloud.spanner.Type;
import com.google.cloud.spanner.Type.Code;
import com.google.cloud.spanner.Value;

import io.datarouter.scanner.Scanner;

public enum SpannerColumnType{
	BOOL(Type.bool(), false, false, Value.bool(null)),
	BOOLEAN_ARRAY(Type.bool(), true, false, Value.boolArray((boolean[])null)),
	BYTES(Type.bytes(), false, true, Value.bytes(null)),
	DATE(Type.date(), false, false, Value.date(null)),
	DATE_ARRAY(Type.date(), true, false, Value.dateArray(null)),
	FLOAT64(Type.float64(), false, false, Value.float64(null)),
	FLOAT64_ARRAY(Type.float64(), true, false, Value.float64Array((double[])null)),
	INT64(Type.int64(), false, false, Value.int64(null)),
	INT64_ARRAY(Type.int64(), true, false, Value.int64Array((long[])null)),
	STRING(Type.string(), false, true, Value.string(null)),
	STRING_ARRAY(Type.string(), true, true, Value.stringArray(null)),
	TIMESTAMP(Type.timestamp(), false, false, Value.timestamp(null)),
	TIMESTAMP_ARRAY(Type.timestamp(), true, false, Value.timestampArray(null)),
	;

	private static final Set<Type> SPANNER_TYPES = Set.of(
			Type.array(Type.bool()),
			Type.array(Type.bytes()),
			Type.array(Type.date()),
			Type.array(Type.float64()),
			Type.array(Type.int64()),
			Type.array(Type.string()),
			Type.array(Type.timestamp()),
			Type.bool(),
			Type.bytes(),
			Type.date(),
			Type.float64(),
			Type.int64(),
			Type.string(),
			Type.timestamp());

	private static final Map<String,Type> SPANNER_TYPES_MAP = Scanner.of(SPANNER_TYPES)
			.toMapSupplied(Type::toString, LinkedHashMap::new);

	private final Type spannerType;
	private final Boolean isArray;
	private final Boolean requiresLength;
	private final Value nullValue;

	SpannerColumnType(Type spannerType, Boolean isArray, Boolean requiresLength, Value nullValue){
		this.spannerType = spannerType;
		this.isArray = isArray;
		this.requiresLength = requiresLength;
		this.nullValue = nullValue;
	}

	public Type getSpannerType(){
		return spannerType;
	}

	public Boolean isArray(){
		return isArray;
	}

	public Boolean requiresLength(){
		return requiresLength;
	}

	public Value nullValue(){
		return nullValue;
	}

	public static SpannerColumnType fromSchemaString(String column){
		String trimmed = column.replace("(MAX)", "");
		Type type = SPANNER_TYPES_MAP.get(trimmed);
		boolean isArray = type.getCode() == Code.ARRAY;
		if(isArray){
			type = type.getArrayElementType();
		}
		for(SpannerColumnType columnType : values()){
			if(columnType.spannerType == type && columnType.isArray == isArray){
				return columnType;
			}
		}
		return null;
	}

}

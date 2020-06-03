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

import java.util.Map;
import java.util.Set;

import com.google.cloud.spanner.Type;
import com.google.cloud.spanner.Type.Code;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.collector.RelaxedMapCollector;

public enum SpannerColumnType{
	BOOL(Type.bool(), false, false),
	BOOLEAN_ARRAY(Type.bool(), true, false),
	BYTES(Type.bytes(), false, true),
	DATE(Type.date(), false, false),
	DATE_ARRAY(Type.date(), true, false),
	FLOAT64(Type.float64(), false, false),
	FLOAT64_ARRAY(Type.float64(), true, false),
	INT64(Type.int64(), false, false),
	INT64_ARRAY(Type.int64(), true, false),
	STRING(Type.string(), false, true),
	STRING_ARRAY(Type.string(), true, true),
	TIMESTAMP(Type.timestamp(), false, false),
	TIMESTAMP_ARRAY(Type.timestamp(), true, false),
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
			.collect(RelaxedMapCollector.of(Type::toString));

	private final Type spannerType;
	private final Boolean isArray;
	private final Boolean requiresLength;

	SpannerColumnType(Type spannerType, Boolean isArray, Boolean requiresLength){
		this.spannerType = spannerType;
		this.isArray = isArray;
		this.requiresLength = requiresLength;
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

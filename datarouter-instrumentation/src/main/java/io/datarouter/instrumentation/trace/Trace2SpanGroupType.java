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
package io.datarouter.instrumentation.trace;

import io.datarouter.enums.MappedEnum;
import io.datarouter.enums.StringMappedEnum;

public enum Trace2SpanGroupType{
	DATABASE(
			0,
			TraceSpanGroupType.DATABASE,
			"database"),
	HTTP(
			1,
			TraceSpanGroupType.HTTP,
			"http call"),
	SERIALIZATION(
			2,
			TraceSpanGroupType.SERIALIZATION,
			"serialization/deserialization"),
	CLOUD_STORAGE(
			3,
			TraceSpanGroupType.CLOUD_STORAGE,
			"cloud storage(e.g. s3, gcs and etc)"),
	MULTITHREADING(
			4,
			TraceSpanGroupType.MULTITHREADING,
			"multithreading"),
	NONE(
			5,
			TraceSpanGroupType.NONE,
			"no group categrized");

	public static final MappedEnum<Trace2SpanGroupType,Integer> BY_PERSISTENT_INT
			= new MappedEnum<>(values(), value -> value.persistentInt);
	public static final StringMappedEnum<Trace2SpanGroupType> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.persistentString);

	public final int persistentInt;
	public final String persistentString;
	public final String description;

	Trace2SpanGroupType(
			int persistentInt,
			TraceSpanGroupType type,
			String description){
		this.persistentInt = persistentInt;
		this.persistentString = type.type;
		this.description = description;
	}

}

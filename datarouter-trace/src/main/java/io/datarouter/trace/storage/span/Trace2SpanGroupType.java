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
package io.datarouter.trace.storage.span;

import io.datarouter.enums.StringMappedEnum;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;

public enum Trace2SpanGroupType{
	DATABASE(TraceSpanGroupType.DATABASE, "database"),
	HTTP(TraceSpanGroupType.HTTP, "http call"),
	SERIALIZATION(TraceSpanGroupType.SERIALIZATION, "serialization/deserialization"),
	CLOUD_STORAGE(TraceSpanGroupType.CLOUD_STORAGE, "cloud storage(e.g. s3, gcs and etc)"),
	MULTITHREADING(TraceSpanGroupType.MULTITHREADING, "multithreading"),
	NONE(TraceSpanGroupType.NONE, "no group categrized");

	public static final StringMappedEnum<Trace2SpanGroupType> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.persistentString);

	public final String persistentString;
	public final String description;

	Trace2SpanGroupType(TraceSpanGroupType type, String description){
		this.persistentString = type.type;
		this.description = description;
	}

}

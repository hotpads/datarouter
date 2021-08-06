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

import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.StringEnum;

public enum Trace2SpanGroupType implements StringEnum<Trace2SpanGroupType>{
	DATABASE(TraceSpanGroupType.DATABASE, "database"),
	HTTP(TraceSpanGroupType.HTTP, "http call"),
	SERIALIZATION(TraceSpanGroupType.SERIALIZATION, "serialization/deserialization"),
	CLOUD_STORAGE(TraceSpanGroupType.CLOUD_STORAGE, "cloud storage(e.g. s3, gcs and etc)"),
	NONE(TraceSpanGroupType.NONE, "no group categrized"),
	;

	private final String persistentString;
	private final String description;

	Trace2SpanGroupType(TraceSpanGroupType type, String description){
		this.persistentString = type.type;
		this.description = description;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	public String getDescription(){
		return description;
	}

	@Override
	public Trace2SpanGroupType fromPersistentString(String string){
		return fromPersistentStringStatic(string);
	}

	public static Trace2SpanGroupType fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

}

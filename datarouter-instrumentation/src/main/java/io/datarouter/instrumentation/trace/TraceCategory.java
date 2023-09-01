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

public enum TraceCategory{
	HTTP_REQUEST(0, "httpRequest"),
	CONVEYOR(1, "conveyor"),
	;

	public static final MappedEnum<TraceCategory,Integer> BY_PERSISTENT_INT
			= new MappedEnum<>(values(), value -> value.persistentInt);
	public static final StringMappedEnum<TraceCategory> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.persistentString);

	public final int persistentInt;
	public final String persistentString;

	TraceCategory(
			int persistentInt,
			String persistentString){
		this.persistentInt = persistentInt;
		this.persistentString = persistentString;
	}

}

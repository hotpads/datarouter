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
package io.datarouter.instrumentation.metric;

import io.datarouter.enums.StringMappedEnum;

public enum MetricAnnotationLevel{
	INFO("INFO"),
	WARN("WARN"),
	ERROR("ERROR"),
	DEBUG("DEBUG");

	public static final StringMappedEnum<MetricAnnotationLevel> BY_NAME
			= new StringMappedEnum<>(values(), Enum::name);
	public static final StringMappedEnum<MetricAnnotationLevel> BY_PERSISTENT_STRING =
			new StringMappedEnum<>(values(), value -> value.persistentString);
	public final String persistentString;

	MetricAnnotationLevel(String persistentString){
		this.persistentString = persistentString;
	}

}

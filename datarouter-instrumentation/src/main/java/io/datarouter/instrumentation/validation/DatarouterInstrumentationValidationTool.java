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
package io.datarouter.instrumentation.validation;

import org.slf4j.Logger;

public class DatarouterInstrumentationValidationTool{
	public static final String PLACEHOLDER = "trimmed";

	public static String trimToSizeAndLog(
			String str,
			int size,
			Logger logger,
			String logDetails){
		int initialLength = str == null ? 0 : str.length();
		if(initialLength <= size){
			return str;
		}
		logger.warn("Trimmed string from={}, to={}, details={}", initialLength, size, logDetails);
		if(size <= PLACEHOLDER.length()){
			return str.substring(0, size);
		}
		return str.substring(0, size - PLACEHOLDER.length()) + PLACEHOLDER;
	}

	public static void throwIfExceedsMaxSize(String value, int maxSize, String fieldName){
		if(value.length() > maxSize){
			throw new IllegalArgumentException(
					String.format("Field [%s] is larger than max size [%d]: [%s]", fieldName, maxSize, value));
		}
	}
}

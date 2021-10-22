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
package io.datarouter.exception.storage.httprecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.util.string.StringTool;

public class FieldTrimTool{
	private static final Logger logger = LoggerFactory.getLogger(FieldTrimTool.class);

	private static final String TRIMMING_REPLACEMENT = "trimmed";
	private static final int TRIMMING_REPLACEMENT_LENGTH = TRIMMING_REPLACEMENT.length();

	public static String trimField(StringFieldKey fieldKey, String field, String databeanId){
		if(field == null){
			return field;
		}
		int fieldSize = fieldKey.getSize();
		int fieldValueLength = field.length();
		if(fieldValueLength > fieldSize){
			logger.warn("Trimmed {} to {} from {}, {}", fieldKey.getName(), fieldSize,
					fieldValueLength, databeanId);
			return StringTool.trimToSize(field, fieldSize - TRIMMING_REPLACEMENT_LENGTH) + TRIMMING_REPLACEMENT;
		}
		return field;
	}

}

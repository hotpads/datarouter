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

import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.util.string.StringTool;

public class FieldTrimTool{

	public static final String TRIMMING_REPLACEMENT = "trimmed";

	public static String trimField(StringFieldKey fieldKey, String field, String databeanId){
		return StringTool.trimToSizeAndLog(
				field,
				fieldKey.getSize(),
				TRIMMING_REPLACEMENT,
				"field=" + fieldKey.getName(),
				databeanId);
	}

}

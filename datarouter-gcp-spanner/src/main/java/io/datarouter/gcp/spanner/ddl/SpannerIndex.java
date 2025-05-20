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
package io.datarouter.gcp.spanner.ddl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.model.field.Field;
import io.datarouter.scanner.Scanner;

public record SpannerIndex(
		String tableName,
		String indexName,
		List<Field<?>> keyFields,
		List<Field<?>> allFields,
		Boolean isUnique){

	public List<Field<?>> getNonKeyFields(){
		if(keyFields.size() == allFields.size()){
			return List.of();
		}
		Map<String,Field<?>> keyMap = Scanner.of(keyFields)
				.toMapSupplied(field -> field.getKey().getColumnName(), LinkedHashMap::new);
		Map<String,Field<?>> nonKeyFieldsMap = Scanner.of(allFields)
				.toMapSupplied(field -> field.getKey().getColumnName(), LinkedHashMap::new);
		keyMap.forEach((key, _) -> nonKeyFieldsMap.remove(key));
		return new ArrayList<>(nonKeyFieldsMap.values());
	}

}

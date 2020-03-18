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
package io.datarouter.client.mysql;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.util.collection.CollectionTool;

public class MysqlUniqueIndexTool{
	private static final Logger logger = LoggerFactory.getLogger(MysqlUniqueIndexTool.class);

	public static <PK extends PrimaryKey<PK>> String searchIndex(
			Map<String,List<Field<?>>> uniqueIndexes,
			Collection<? extends UniqueKey<PK>> keys){
		Optional<? extends UniqueKey<PK>> findFirst = CollectionTool.findFirst(keys);
		if(findFirst.isEmpty()){
			logger.warn("no keys to guess the index", new Exception());
			return null;
		}
		List<?> dataFieldKeys = findFirst.get().getFields().stream()
				.map(Field::getKey)
				.collect(Collectors.toList());
		for(Entry<String,List<Field<?>>> uniqueIndex : uniqueIndexes.entrySet()){
			List<?> defFieldKeys = uniqueIndex.getValue().stream()
					.map(Field::getKey)
					.collect(Collectors.toList());
			if(defFieldKeys.equals(dataFieldKeys)){
				return uniqueIndex.getKey();
			}
		}
		logger.warn("matching index not found uniqueIndexes={} findFirst={}",
				uniqueIndexes,
				findFirst,
				new Exception());
		return null;
	}

}

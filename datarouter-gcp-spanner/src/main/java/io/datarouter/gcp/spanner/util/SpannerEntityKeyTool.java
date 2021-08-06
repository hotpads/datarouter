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
package io.datarouter.gcp.spanner.util;

import java.util.List;

import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Key.Builder;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class SpannerEntityKeyTool{

	public static <
			EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>>
	Builder getPartiton(PK key, EntityPartitioner<EK> partitioner){
		Builder keyBuilder = Key.newBuilder();
		keyBuilder.append(partitioner.getPartition(key.getEntityKey()));
		return keyBuilder;
	}

	public static String getEntityTableName(PhysicalDatabeanFieldInfo<?,?,?> fieldInfo){
		return fieldInfo.getSampleDatabean().getDatabeanName() + "_" + Math.abs(fieldInfo.getTableName().hashCode())
				% 1000;
	}

	public static <PK extends PrimaryKey<PK>> List<Field<?>> getPrimaryKeyFields(PK key, boolean isEntity){
		if(isEntity && key instanceof EntityPrimaryKey){
			EntityPrimaryKey<?,?> entityPrimaryKey = (EntityPrimaryKey<?,?>)key;
			return Scanner.of(entityPrimaryKey.getEntityKeyFields(), entityPrimaryKey.getPostEntityKeyFields())
					.concat(Scanner::of)
					.list();
		}
		return key.getFields();
	}

}

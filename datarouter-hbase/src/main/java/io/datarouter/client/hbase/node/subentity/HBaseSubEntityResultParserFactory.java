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
package io.datarouter.client.hbase.node.subentity;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.util.lang.ReflectionTool;

@Singleton
public class HBaseSubEntityResultParserFactory{

	public static <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	HBaseSubEntityResultParser<EK,PK,D> create(
			EntityFieldInfo<EK,E> entityFieldInfo,
			DatabeanFieldInfo<PK,D,F> fieldInfo){
		Supplier<D> databeanSupplier = fieldInfo.getDatabeanSupplier();
		int prefixByteLength = entityFieldInfo.getEntityPartitioner().getNumPrefixBytes();
		return new HBaseSubEntityResultParser<>(
				fieldInfo.getPrimaryKeySupplier(),
				entityFieldInfo.getEntityKeySupplier(),
				fieldInfo.getEkFields(),
				fieldInfo.getEkPkFields(),
				fieldInfo.getPostEkPkKeyFields(),
				fieldInfo.getNonKeyFieldByColumnName(),
				prefixByteLength,
				fieldInfo.getEntityColumnPrefixBytes(),
				fieldInfo.getKeyJavaField(),
				databeanSupplier);
	}

	public static <EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	HBaseSubEntityResultParser<EK,PK,D> create(
			Supplier<EK> entityKeySupplier,
			Supplier<PK> primaryKeySupplier,
			Supplier<D> databeanSupplier,
			F databeanFielder,
			int numPrefixBytes,
			byte[] entityColumnPrefixBytes){

		D prototypeDatabean = databeanSupplier.get();
		PK primaryKey = prototypeDatabean.getKey();

		Map<String,Field<?>> nonKeyFieldsByColumnNames = databeanFielder.getNonKeyFields(prototypeDatabean).stream()
				.collect(Collectors.toMap(field -> field.getKey().getColumnName(), Function.identity()));

		java.lang.reflect.Field keyJavaField = ReflectionTool.getDeclaredFieldFromAncestors(
				prototypeDatabean.getClass(), prototypeDatabean.getKeyFieldName());

		return new HBaseSubEntityResultParser<>(
				primaryKeySupplier,
				entityKeySupplier,
				primaryKey.getEntityKey().getFields(),
				primaryKey.getEntityKeyFields(),
				primaryKey.getPostEntityKeyFields(),
				nonKeyFieldsByColumnNames,
				numPrefixBytes,
				entityColumnPrefixBytes,
				keyJavaField,
				databeanSupplier);
	}

}

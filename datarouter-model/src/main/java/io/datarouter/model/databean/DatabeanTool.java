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
package io.datarouter.model.databean;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.tuple.Pair;

public class DatabeanTool{

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> D create(Class<D> databeanClass){
		try{
			// use getDeclaredConstructor to access non-public constructors
			Constructor<D> constructor = databeanClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			D databeanInstance = constructor.newInstance();
			return databeanInstance;
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName() + " on " + databeanClass.getSimpleName()
					+ ". Is there a no-arg constructor?");
		}
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> byte[] getBytes(
			D databean,
			DatabeanFielder<PK,D> fielder){
		return getBytes(fielder.getKeyFields(databean), fielder.getNonKeyFields(databean));
	}

	protected static byte[] getBytes(List<Field<?>> keyFields, List<Field<?>> nonKeyFields){
		// always include zero-length fields in key bytes
		byte[] keyBytes = FieldTool.getSerializedKeyValues(keyFields, true, false);
		// skip zero-length fields in non-key bytes
		// TODO should this distinguish between null and empty Strings?
		byte[] nonKeyBytes = FieldTool.getSerializedKeyValues(nonKeyFields, true, true);
		byte[] allBytes = ArrayTool.concatenate(keyBytes, nonKeyBytes);
		return allBytes;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	String getColumnNames(Class<D> databean, Class<F> basedataBeanFielder){
		String columns = "";
		D emptyDatabean = DatabeanTool.create(databean);
		DatabeanFielder<PK,D> databeanFielder = ReflectionTool.create(basedataBeanFielder);
		List<Field<?>> dataBeanFields = databeanFielder.getFields(emptyDatabean);
		for(int i = 0; i < dataBeanFields.size(); i++){
			Field<?> field = dataBeanFields.get(i);
			columns = columns + field.getKey().getColumnName() + ",";
		}
		columns = columns.substring(0, columns.length() - 1);
		return columns;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> String getCsvColumnNames(
			D databean,
			DatabeanFielder<PK,D> fielder){
		return FieldTool.getCsvColumnNames(fielder.getFields(databean));
	}

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Map<String,Pair<Field<?>,Field<?>>> getFieldDifferences(
			D databean1,
			D databean2,
			Supplier<F> fielderSupplier){
		return getFieldDifferencesWithExclusions(databean1, databean2, fielderSupplier, Collections.emptySet());
	}

	public static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	Map<String,Pair<Field<?>,Field<?>>> getFieldDifferencesWithExclusions(
			D databean1,
			D databean2,
			Supplier<F> fielderSupplier,
			Set<String> prefixedFieldNameExclusions){
		F fielder = fielderSupplier.get();
		Collection<Field<?>> leftFields = databean1 == null
				? null
				: Scanner.of(fielder.getFields(databean1))
						.exclude(field -> prefixedFieldNameExclusions.contains(field.getPrefixedName()))
						.list();
		Collection<Field<?>> rightFields = databean2 == null
				? null
				: Scanner.of(fielder.getFields(databean2))
						.exclude(field -> prefixedFieldNameExclusions.contains(field.getPrefixedName()))
						.list();
		return FieldSetTool.getFieldDifferences(leftFields, rightFields);
	}

}

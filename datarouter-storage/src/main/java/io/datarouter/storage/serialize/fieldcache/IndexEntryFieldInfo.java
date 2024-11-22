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
package io.datarouter.storage.serialize.fieldcache;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.util.lang.ReflectionTool;

public class IndexEntryFieldInfo<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final String indexName;
	private final Supplier<D> databeanSupplier;
	private final Supplier<F> fielderSupplier;
	private final D sampleDatabean;
	private final java.lang.reflect.Field keyJavaField;
	private final F sampleFielder;
	private final Supplier<PK> primaryKeySupplier;
	private final List<Field<?>> fields;
	private final List<Field<?>> primaryKeyFields;
	private final List<Field<?>> nonKeyFields;
	private final List<String> primaryKeyFieldColumnNames;
	private final List<String> fieldColumnNames;

	public IndexEntryFieldInfo(String indexName, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
		this.indexName = indexName;
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
		this.sampleDatabean = databeanSupplier.get();
		this.keyJavaField = ReflectionTool.getDeclaredFieldFromAncestors(
				sampleDatabean.getClass(),
				sampleDatabean.getKeyFieldName());
		this.sampleFielder = fielderSupplier.get();
		this.primaryKeySupplier = sampleDatabean.getKeySupplier();
		this.fields = sampleFielder.getFields(sampleDatabean);
		this.primaryKeyFields = primaryKeySupplier.get().getFields();
		this.nonKeyFields = sampleFielder.getNonKeyFields(sampleDatabean);
		this.primaryKeyFieldColumnNames = createFieldColumnNames(this.primaryKeyFields);
		this.fieldColumnNames = createFieldColumnNames(this.fields);
	}

	private List<String> createFieldColumnNames(List<Field<?>> fields){
		return fields.stream()
				.map(Field::getKey)
				.map(FieldKey::getColumnName)
				.toList();
	}

	public String getIndexName(){
		return indexName;
	}

	public Supplier<D> getDatabeanSupplier(){
		return databeanSupplier;
	}

	public Supplier<F> getFielderSupplier(){
		return fielderSupplier;
	}

	public D getSampleDatabean(){
		return sampleDatabean;
	}

	public java.lang.reflect.Field getKeyJavaField(){
		return keyJavaField;
	}

	public F getSampleFielder(){
		return sampleFielder;
	}

	public Supplier<PK> getPrimaryKeySupplier(){
		return primaryKeySupplier;
	}

	public List<Field<?>> getFields(){
		return fields;
	}

	public List<Field<?>> getPrimaryKeyFields(){
		return primaryKeyFields;
	}

	public List<Field<?>> getNonKeyFields(){
		return nonKeyFields;
	}

	public List<String> getPrimaryKeyFieldColumnNames(){
		return primaryKeyFieldColumnNames;
	}

	public List<String> getFieldColumnNames(){
		return fieldColumnNames;
	}

}

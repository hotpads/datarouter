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
package io.datarouter.model.serialize.fielder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.codec.JsonDatabeanCodec;
import io.datarouter.util.lang.ReflectionTool;

public abstract class BaseDatabeanFielder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements DatabeanFielder<PK,D>{

	private final Fielder<PK> primaryKeyFielder;
	private final StringDatabeanCodec stringDatabeanCodec;
	private final Map<FielderConfigKey<?>,FielderConfigValue<?>> configuration;

	@Deprecated // use BaseDatabeanFielder(supplier)
	protected BaseDatabeanFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
		this(ReflectionTool.supplier(primaryKeyFielderClass));
	}

	protected BaseDatabeanFielder(Supplier<? extends Fielder<PK>> primaryKeyFielderSupplier){
		this.primaryKeyFielder = primaryKeyFielderSupplier.get();
		this.stringDatabeanCodec = ReflectionTool.create(getStringDatabeanCodecClass());
		this.configuration = new HashMap<>();
		configure();
	}

	@Override
	public Fielder<PK> getKeyFielder(){
		return primaryKeyFielder;
	}

	@Override
	public List<Field<?>> getKeyFields(D databean){
		return FieldTool.prependPrefixes(databean.getKeyFieldName(), primaryKeyFielder.getFields(databean.getKey()));
	}

	@Override
	public List<Field<?>> getFields(D databean){
		List<Field<?>> allFields = new ArrayList<>();
		allFields.addAll(getKeyFields(databean)); //getKeyFields already prepends prefixes
		allFields.addAll(getNonKeyFields(databean));
		return allFields;
	}

	@Override
	public Map<String,List<Field<?>>> getUniqueIndexes(D databean){
		return new TreeMap<>();
	}

	@Override
	public final void addOption(FielderConfigValue<?> fielderConfigValue){
		configuration.put(fielderConfigValue.getKey(), fielderConfigValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T extends FielderConfigValue<T>> Optional<T> getOption(FielderConfigKey<T> key){
		return Optional.ofNullable((T)configuration.get(key));
	}

	@Override
	public Collection<FielderConfigValue<?>> getOptions(){
		return configuration.values();
	}

	@Override
	public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
		return JsonDatabeanCodec.class;
	}

	@Override
	public final StringDatabeanCodec getStringDatabeanCodec(){
		return stringDatabeanCodec;
	}

	@Override
	public Optional<Long> getTtlMs(){
		return Optional.empty();
	}

}

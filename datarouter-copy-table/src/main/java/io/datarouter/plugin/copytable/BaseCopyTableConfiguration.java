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
package io.datarouter.plugin.copytable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.util.Require;

public abstract class BaseCopyTableConfiguration implements CopyTableConfiguration{

	private final Map<String,Predicate<?>> filterByName;
	private final Map<String,BiConsumer<?,Config>> processorByName;

	public BaseCopyTableConfiguration(){
		this.filterByName = new TreeMap<>();
		this.processorByName = new TreeMap<>();
	}

	@Override
	public List<String> getFilterNames(){
		return new ArrayList<>(filterByName.keySet());
	}

	@Override
	public Optional<Predicate<?>> findFilter(String name){
		return Optional.ofNullable(name)
				.map(filterByName::get);
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	BaseCopyTableConfiguration registerFilter(String name, Predicate<D> filter){
		Require.notContains(filterByName.keySet(), name);
		filterByName.put(name, filter);
		return this;
	}

	@Override
	public Optional<BiConsumer<?,Config>> findProcessor(String processorName){
		return Optional.ofNullable(processorName)
				.map(processorByName::get);
	}


	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	BaseCopyTableConfiguration registerProcessor(String name, BiConsumer<List<D>,Config> processor){
		Require.notContains(processorByName.keySet(), name);
		processorByName.put(name, processor);
		return this;
	}

}

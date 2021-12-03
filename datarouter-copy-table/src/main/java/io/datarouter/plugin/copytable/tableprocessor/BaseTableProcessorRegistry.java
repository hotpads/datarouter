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
package io.datarouter.plugin.copytable.tableprocessor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.Require;

public abstract class BaseTableProcessorRegistry implements TableProcessorRegistry{

	private final Map<String,Class<? extends TableProcessor<?,?>>> processorByName;

	public BaseTableProcessorRegistry(){
		this.processorByName = new TreeMap<>();
	}

	@Override
	public Optional<Class<? extends TableProcessor<?,?>>> find(String processorName){
		return Optional.ofNullable(processorName).map(processorByName::get);
	}

	@Override
	public Collection<Class<? extends TableProcessor<?,?>>> getAll(){
		return processorByName.values();
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	BaseTableProcessorRegistry register(
			Class<? extends TableProcessor<PK,D>> processorClass){
		String name = processorClass.getSimpleName();
		Require.notContains(processorByName.keySet(), name);
		processorByName.put(name, processorClass);
		return this;
	}

}

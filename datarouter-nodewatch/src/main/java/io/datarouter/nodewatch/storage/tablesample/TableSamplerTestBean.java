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
package io.datarouter.nodewatch.storage.tablesample;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class TableSamplerTestBean extends BaseDatabean<TableSamplerTestBeanKey,TableSamplerTestBean>{

	public static class TableSamplerTestBeanFielder
	extends BaseDatabeanFielder<TableSamplerTestBeanKey,TableSamplerTestBean>{

		public TableSamplerTestBeanFielder(){
			super(TableSamplerTestBeanKey::new);
		}
		@Override
		public List<Field<?>> getNonKeyFields(TableSamplerTestBean databean){
			return List.of();
		}

	}

	@Override
	public Supplier<TableSamplerTestBeanKey> getKeySupplier(){
		return TableSamplerTestBeanKey::new;
	}

	public TableSamplerTestBean(){
		super(new TableSamplerTestBeanKey(null, null, null));
	}

	public TableSamplerTestBean(Long fieldA, Long fieldB, String other){
		super(new TableSamplerTestBeanKey(fieldA, fieldB, other));
	}

}

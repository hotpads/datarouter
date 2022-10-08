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
package io.datarouter.model.databean;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

/**
 * This is a placeholder to satisfy generic parameters
 */
public class EmptyDatabean extends BaseDatabean<EmptyDatabeanKey,EmptyDatabean>{

	public static class EmptyDatabeanFielder
	extends BaseDatabeanFielder<EmptyDatabeanKey,EmptyDatabean>{

		public EmptyDatabeanFielder(){
			super(EmptyDatabeanKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(EmptyDatabean databean){
			return List.of();
		}

	}

	public EmptyDatabean(){
		super(new EmptyDatabeanKey());
	}

	@Override
	public Supplier<EmptyDatabeanKey> getKeySupplier(){
		return EmptyDatabeanKey::new;
	}

}

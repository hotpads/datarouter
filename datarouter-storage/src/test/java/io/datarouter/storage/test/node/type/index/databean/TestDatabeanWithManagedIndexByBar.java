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
package io.datarouter.storage.test.node.type.index.databean;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.index.unique.UniqueIndexEntry;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanKey;

public class TestDatabeanWithManagedIndexByBar
extends BaseDatabean<TestDatabeanWithManagedIndexByBarKey,TestDatabeanWithManagedIndexByBar>
implements UniqueIndexEntry<
		TestDatabeanWithManagedIndexByBarKey,
		TestDatabeanWithManagedIndexByBar,
		TestDatabeanKey,
		TestDatabean>{

	private String foo;

	public static class TestDatabeanWithManagedIndexByBFielder
	extends BaseDatabeanFielder<TestDatabeanWithManagedIndexByBarKey,TestDatabeanWithManagedIndexByBar>{

		public TestDatabeanWithManagedIndexByBFielder(){
			super(TestDatabeanWithManagedIndexByBarKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TestDatabeanWithManagedIndexByBar databean){
			return List.of(new StringField(TestDatabeanKey.FieldKeys.foo, databean.foo));
		}

	}

	public TestDatabeanWithManagedIndexByBar(){
		super(new TestDatabeanWithManagedIndexByBarKey());
	}

	public TestDatabeanWithManagedIndexByBar(String bar, String foo){
		super(new TestDatabeanWithManagedIndexByBarKey(bar));
		this.foo = foo;
	}

	@Override
	public Supplier<TestDatabeanWithManagedIndexByBarKey> getKeySupplier(){
		return TestDatabeanWithManagedIndexByBarKey::new;
	}

	@Override
	public TestDatabeanKey getTargetKey(){
		return new TestDatabeanKey(foo);
	}

	@Override
	public List<TestDatabeanWithManagedIndexByBar> createFromDatabean(TestDatabean target){
		TestDatabeanWithManagedIndexByBar entry = new TestDatabeanWithManagedIndexByBar(target.getBar(), target.getKey()
				.getFoo());
		return Collections.singletonList(entry);
	}

	public String getFoo(){
		return foo;
	}

}

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

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.key.primary.BasePrimaryKey;
import io.datarouter.storage.test.TestDatabean;

public class TestDatabeanWithManagedIndexByBarKey extends BasePrimaryKey<TestDatabeanWithManagedIndexByBarKey>{

	private String bar;

	public TestDatabeanWithManagedIndexByBarKey(){
		this(null);
	}

	public TestDatabeanWithManagedIndexByBarKey(String bar){
		this.bar = bar;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(TestDatabean.FieldKeys.bar, bar));
	}

	public String getBar(){
		return bar;
	}

}

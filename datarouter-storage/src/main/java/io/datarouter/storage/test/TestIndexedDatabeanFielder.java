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
package io.datarouter.storage.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.key.unique.BaseUniqueKey;


public class TestIndexedDatabeanFielder extends TestDatabeanFielder{

	@Override
	public Map<String, List<Field<?>>> getUniqueIndexes(TestDatabean databean){
		Map<String, List<Field<?>>> indexes = new HashMap<>();
		indexes.put("byBaz", new TestDatabeanByBazLookup(databean.getBaz()).getFields());
		return indexes;
	}

	public static class TestDatabeanByBazLookup extends BaseUniqueKey<TestDatabeanKey>{

		private String baz;

		public TestDatabeanByBazLookup(String baz){
			this.baz = baz;
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(new StringField(TestDatabean.FieldKeys.baz, baz));
		}

	}

}
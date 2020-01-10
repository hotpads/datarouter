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
package io.datarouter.client.hbase.test.entity.databean;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.entity.base.BaseEntityKey;

public class HBaseBeanTestEntityKey extends BaseEntityKey<HBaseBeanTestEntityKey>{

	private String foo;
	private String bar;

	public static class FieldKeys{
		public static final StringFieldKey foo = new StringFieldKey("foo");
		public static final StringFieldKey bar = new StringFieldKey("bar");

	}

	public HBaseBeanTestEntityKey(){
	}

	public HBaseBeanTestEntityKey(String foo, String bar){
		this.foo = foo;
		this.bar = bar;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.foo, foo),
				new StringField(FieldKeys.bar, bar));
	}

	public String getFoo(){
		return foo;
	}

	public String getBar(){
		return bar;
	}

}

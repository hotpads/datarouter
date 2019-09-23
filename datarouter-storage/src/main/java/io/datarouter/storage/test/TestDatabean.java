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

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.imp.StringFieldKey;

public class TestDatabean extends BaseDatabean<TestDatabeanKey, TestDatabean>{

	private String bar;
	private String baz;

	public static class FieldKeys{
		public static final StringFieldKey bar = new StringFieldKey("bar");
		public static final StringFieldKey baz = new StringFieldKey("baz");
	}

	public TestDatabean(){
		super(new TestDatabeanKey());
	}

	public TestDatabean(String foo, String bar, String baz){
		super(new TestDatabeanKey(foo));
		this.bar = bar;
		this.baz = baz;
	}

	@Override
	public Class<TestDatabeanKey> getKeyClass(){
		return TestDatabeanKey.class;
	}

	public String getBar(){
		return bar;
	}

	public String getBaz(){
		return baz;
	}

}

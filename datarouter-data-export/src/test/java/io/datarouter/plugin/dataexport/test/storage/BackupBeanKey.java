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
package io.datarouter.plugin.dataexport.test.storage;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class BackupBeanKey extends BaseRegularPrimaryKey<BackupBeanKey>{

	private String foo;
	private String bar;
	private Integer baz;
	private String qux;

	public static class FieldKeys{
		public static final StringFieldKey foo = new StringFieldKey("foo");
		public static final StringFieldKey bar = new StringFieldKey("bar");
		public static final IntegerFieldKey baz = new IntegerFieldKey("baz");
		public static final StringFieldKey qux = new StringFieldKey("qux");
	}

	public BackupBeanKey(){
	}

	public BackupBeanKey(String foo, String bar, Integer baz, String qux){
		this.foo = foo;
		this.bar = bar;
		this.baz = baz;
		this.qux = qux;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.foo, foo),
				new StringField(FieldKeys.bar, bar),
				new IntegerField(FieldKeys.baz, baz),
				new StringField(FieldKeys.qux, qux));
	}

}

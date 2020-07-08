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
package io.datarouter.client.mysql.test.client.insert.generated.managed;

import java.util.List;

import io.datarouter.client.mysql.test.client.insert.generated.PutOpGeneratedTestBean;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class PutOpIdGeneratedManagedTestBean
extends BaseDatabean<PutOpIdGeneratedManagedTestBeanKey,PutOpIdGeneratedManagedTestBean>
implements PutOpGeneratedTestBean<PutOpIdGeneratedManagedTestBeanKey,PutOpIdGeneratedManagedTestBean>{

	private String foo;

	public static class FieldKeys{
		public static final StringFieldKey foo = new StringFieldKey("foo").withSize(10);
	}

	public static class PutOpIdGeneratedManagedTestBeanFielder
	extends BaseDatabeanFielder<PutOpIdGeneratedManagedTestBeanKey,PutOpIdGeneratedManagedTestBean>{

		public PutOpIdGeneratedManagedTestBeanFielder(){
			super(PutOpIdGeneratedManagedTestBeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(PutOpIdGeneratedManagedTestBean bean){
			return List.of(new StringField(FieldKeys.foo, bean.foo));
		}

	}

	public PutOpIdGeneratedManagedTestBean(){
		super(new PutOpIdGeneratedManagedTestBeanKey());
	}

	public PutOpIdGeneratedManagedTestBean(String str){
		super(new PutOpIdGeneratedManagedTestBeanKey());
		this.foo = str;
	}

	@Override
	public Class<PutOpIdGeneratedManagedTestBeanKey> getKeyClass(){
		return PutOpIdGeneratedManagedTestBeanKey.class;
	}

}

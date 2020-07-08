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
package io.datarouter.client.mysql.test.client.insert.generated.random;

import java.util.List;

import io.datarouter.client.mysql.test.client.insert.generated.PutOpGeneratedTestBean;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class PutOpIdGeneratedRandomTestBean
extends BaseDatabean<PutOpIdGeneratedRandomTestBeanKey,PutOpIdGeneratedRandomTestBean>
implements PutOpGeneratedTestBean<PutOpIdGeneratedRandomTestBeanKey,PutOpIdGeneratedRandomTestBean>{

	private String aa;

	public static class FieldKeys{
		public static final StringFieldKey aa = new StringFieldKey("aa").withSize(10);
	}

	public static class PutOpIdGeneratedRandomTestBeanFielder
	extends BaseDatabeanFielder<PutOpIdGeneratedRandomTestBeanKey,PutOpIdGeneratedRandomTestBean>{

		public PutOpIdGeneratedRandomTestBeanFielder(){
			super(PutOpIdGeneratedRandomTestBeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(PutOpIdGeneratedRandomTestBean val){
			return List.of(new StringField(FieldKeys.aa, val.aa));
		}

	}

	public PutOpIdGeneratedRandomTestBean(){
		super(new PutOpIdGeneratedRandomTestBeanKey());
	}

	public PutOpIdGeneratedRandomTestBean(String val){
		super(new PutOpIdGeneratedRandomTestBeanKey());
		this.aa = val;
	}

	@Override
	public Class<PutOpIdGeneratedRandomTestBeanKey> getKeyClass(){
		return PutOpIdGeneratedRandomTestBeanKey.class;
	}

}

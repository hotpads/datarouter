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
package io.datarouter.client.mysql.test.client.insert;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class PutOpTestBean extends BaseDatabean<PutOpTestBeanKey,PutOpTestBean>{

	private String strC;

	public static class FieldKeys{
		public static final StringFieldKey strC = new StringFieldKey("strC").withSize(100);
	}

	public PutOpTestBean(){
		super(new PutOpTestBeanKey());
	}

	public PutOpTestBean(String strA, String strB, String strC){
		super(new PutOpTestBeanKey(strA, strB));
		this.strC = strC;
	}

	public static class PutOpTestBeanFielder extends BaseDatabeanFielder<PutOpTestBeanKey,PutOpTestBean>{

		public PutOpTestBeanFielder(){
			super(PutOpTestBeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(PutOpTestBean databean){
			return List.of(new StringField(FieldKeys.strC, databean.strC));
		}

	}

	@Override
	public Supplier<PutOpTestBeanKey> getKeySupplier(){
		return PutOpTestBeanKey::new;
	}

	public String getC(){
		return strC;
	}

	public void setC(String strC){
		this.strC = strC;
	}

}

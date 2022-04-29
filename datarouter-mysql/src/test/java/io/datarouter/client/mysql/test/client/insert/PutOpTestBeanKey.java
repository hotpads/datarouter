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
package io.datarouter.client.mysql.test.client.insert;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class PutOpTestBeanKey extends BaseRegularPrimaryKey<PutOpTestBeanKey>{

	private String first;
	private String second;

	public PutOpTestBeanKey(){
	}

	public PutOpTestBeanKey(String first, String second){
		this.first = first;
		this.second = second;
	}

	public static class FieldKeys{
		public static final StringFieldKey first = new StringFieldKey("first")
				.withSize(50);
		public static final StringFieldKey second = new StringFieldKey("second");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.first, first),
				new StringField(FieldKeys.second, second));
	}

	public String getA(){
		return first;
	}

	public void setA(String first){
		this.first = first;
	}

	public String getB(){
		return second;
	}

	public void setB(String second){
		this.second = second;
	}

}

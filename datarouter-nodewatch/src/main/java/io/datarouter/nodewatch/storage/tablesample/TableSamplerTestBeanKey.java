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
package io.datarouter.nodewatch.storage.tablesample;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class TableSamplerTestBeanKey extends BaseRegularPrimaryKey<TableSamplerTestBeanKey>{

	private Long fieldA;
	private Long fieldB;
	private String other;

	public static class FieldKeys{
		public static final LongFieldKey fieldA = new LongFieldKey("fieldA");
		public static final LongFieldKey fieldB = new LongFieldKey("fieldB");
		public static final StringFieldKey other = new StringFieldKey("other");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new LongField(FieldKeys.fieldA, fieldA),
				new LongField(FieldKeys.fieldB, fieldB),
				new StringField(FieldKeys.other, other));
	}

	public TableSamplerTestBeanKey(){
	}

	public TableSamplerTestBeanKey(Long fieldA, Long fieldB, String other){
		this.fieldA = fieldA;
		this.fieldB = fieldB;
		this.other = other;
	}

	public Long getFieldA(){
		return fieldA;
	}

	public void setFieldA(Long fieldA){
		this.fieldA = fieldA;
	}

	public Long getFieldB(){
		return fieldB;
	}

	public void setFieldB(Long fieldB){
		this.fieldB = fieldB;
	}

	public String getOther(){
		return other;
	}

	public void setOther(String other){
		this.other = other;
	}

}
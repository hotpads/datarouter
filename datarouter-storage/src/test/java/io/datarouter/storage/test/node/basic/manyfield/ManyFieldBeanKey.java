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
package io.datarouter.storage.test.node.basic.manyfield;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.util.number.RandomTool;

public class ManyFieldBeanKey extends BaseRegularPrimaryKey<ManyFieldBeanKey>{

	protected Long id;

	public static class FieldKeys{
		public static final LongFieldKey id = new LongFieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(new LongField(FieldKeys.id, id));
	}

	public ManyFieldBeanKey(){
		this.id = RandomTool.nextPositiveLong();
	}

	public ManyFieldBeanKey(Long id){
		this.id = id;
	}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

}
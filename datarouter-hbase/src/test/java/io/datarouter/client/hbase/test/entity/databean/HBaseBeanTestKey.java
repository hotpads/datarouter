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
package io.datarouter.client.hbase.test.entity.databean;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.base.BaseEntityPrimaryKey;

public class HBaseBeanTestKey extends BaseEntityPrimaryKey<HBaseBeanTestEntityKey,HBaseBeanTestKey>{

	private HBaseBeanTestEntityKey entityKey;
	private String baz;
	private String qux;

	public static class FieldKeys{
		public static final StringFieldKey baz = new StringFieldKey("baz");
		public static final StringFieldKey qux = new StringFieldKey("qux");
	}

	public HBaseBeanTestKey(){
		this.entityKey = new HBaseBeanTestEntityKey();
	}

	public HBaseBeanTestKey(HBaseBeanTestEntityKey entityKey, String baz, String qux){
		this.entityKey = entityKey;
		this.baz = baz;
		this.qux = qux;
	}

	public HBaseBeanTestKey(String foo, String bar, String baz, String qux){
		this(new HBaseBeanTestEntityKey(foo, bar), baz, qux);
	}

	@Override
	public HBaseBeanTestEntityKey getEntityKey(){
		return entityKey;
	}

	@Override
	public HBaseBeanTestKey prefixFromEntityKey(HBaseBeanTestEntityKey entityKey){
		return new HBaseBeanTestKey(entityKey.getFoo(), entityKey.getBar(), null, null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return List.of(
				new StringField(FieldKeys.baz, baz),
				new StringField(FieldKeys.qux, qux));
	}

}

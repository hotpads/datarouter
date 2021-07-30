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

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class HBaseBeanTest extends BaseDatabean<HBaseBeanTestKey,HBaseBeanTest>{

	private String corge;

	public static class FieldKeys{
		public static final StringFieldKey corge = new StringFieldKey("corge");
	}

	public HBaseBeanTest(){
		this(null, null, null, null, null);
	}

	public HBaseBeanTest(HBaseBeanTestKey key, String corge){
		super(key);
		this.corge = corge;
	}

	public HBaseBeanTest(String foo, String bar, String baz, String qux, String corge){
		this(new HBaseBeanTestKey(foo, bar, baz, qux), corge);
	}

	public static class HBaseBeanTestFielder extends BaseDatabeanFielder<HBaseBeanTestKey,HBaseBeanTest>{

		public HBaseBeanTestFielder(){
			super(HBaseBeanTestKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(HBaseBeanTest databean){
			return List.of(new StringField(FieldKeys.corge, databean.corge));
		}

	}

	@Override
	public Supplier<HBaseBeanTestKey> getKeySupplier(){
		return HBaseBeanTestKey::new;
	}

}

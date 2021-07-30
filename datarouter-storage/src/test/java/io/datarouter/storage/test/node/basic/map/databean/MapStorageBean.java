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
package io.datarouter.storage.test.node.basic.map.databean;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class MapStorageBean extends BaseDatabean<MapStorageBeanKey,MapStorageBean>{

	private String data;

	public static class FieldKeys{
		public static final StringFieldKey data = new StringFieldKey("data");
	}

	public static class MapStorageBeanFielder extends BaseDatabeanFielder<MapStorageBeanKey,MapStorageBean>{

		public MapStorageBeanFielder(){
			super(MapStorageBeanKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(MapStorageBean databean){
			return List.of(new StringField(FieldKeys.data, databean.data));
		}

	}

	public MapStorageBean(){
		this(null);
	}

	public MapStorageBean(String data){
		super(new MapStorageBeanKey());
		this.data = data;
	}

	@Override
	public Supplier<MapStorageBeanKey> getKeySupplier(){
		return MapStorageBeanKey::new;
	}

	public String getData(){
		return data;
	}

	public void setData(String data){
		this.data = data;
	}

}

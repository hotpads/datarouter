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

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class MapStorageBean extends BaseDatabean<MapStorageBeanKey,MapStorageBean>{

	private MapStorageBeanKey key;
	private String data;

	/** column names *********************************************************/

	public static class FieldKeys{
		public static final StringFieldKey data = new StringFieldKey("data");
	}

	public static class MapStorageBeanFielder extends BaseDatabeanFielder<MapStorageBeanKey,MapStorageBean>{

		public MapStorageBeanFielder(){
			super(MapStorageBeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(MapStorageBean databean){
			return Arrays.asList(new StringField(FieldKeys.data, databean.data));
		}
	}

	/** constructor **********************************************************/

	public MapStorageBean(){
		this(null);
	}

	public MapStorageBean(String data){
		this.key = new MapStorageBeanKey();
		this.data = data;
	}

	/** databean *************************************************************/

	@Override
	public Class<MapStorageBeanKey> getKeyClass(){
		return MapStorageBeanKey.class;
	}

	@Override
	public MapStorageBeanKey getKey(){
		return key;
	}

	/** get/set **************************************************************/

	public String getData(){
		return data;
	}

	public void setData(String data){
		this.data = data;
	}
}
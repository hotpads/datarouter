package com.hotpads.datarouter.test.node.basic.map.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

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
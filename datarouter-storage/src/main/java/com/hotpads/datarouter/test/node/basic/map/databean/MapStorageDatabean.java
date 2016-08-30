package com.hotpads.datarouter.test.node.basic.map.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class MapStorageDatabean extends BaseDatabean<MapStorageDatabeanKey,MapStorageDatabean>{

	private MapStorageDatabeanKey key;
	private String data;

	/** column names *********************************************************/

	public static class FieldKeys{
		public static final StringFieldKey data = new StringFieldKey("data");
	}

	public static class MapStorageDatabeanFielder extends BaseDatabeanFielder<MapStorageDatabeanKey,MapStorageDatabean>{

		public MapStorageDatabeanFielder(){
			super(MapStorageDatabeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(MapStorageDatabean databean){
			return Arrays.asList(new StringField(FieldKeys.data, databean.data));
		}
	}

	/** constructor **********************************************************/

	public MapStorageDatabean(){
		this(null, null);
	}

	public MapStorageDatabean(String id, String data){
		this.key = new MapStorageDatabeanKey(id);
		this.data = data;
	}

	/** databean *************************************************************/

	@Override
	public Class<MapStorageDatabeanKey> getKeyClass(){
		return MapStorageDatabeanKey.class;
	}

	@Override
	public MapStorageDatabeanKey getKey(){
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
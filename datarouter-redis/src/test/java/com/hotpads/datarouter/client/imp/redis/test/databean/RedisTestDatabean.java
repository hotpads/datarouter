package com.hotpads.datarouter.client.imp.redis.test.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class RedisTestDatabean extends BaseDatabean<RedisTestDatabeanKey, RedisTestDatabean>{

	private RedisTestDatabeanKey key;
	private String data;

	/** column names *********************************************************/

	public static class FieldKeys{
		public static final StringFieldKey data = new StringFieldKey("data");
	}

	public static class RedisTestDatabeanFielder extends BaseDatabeanFielder<RedisTestDatabeanKey, RedisTestDatabean>{

		public RedisTestDatabeanFielder(){
			super(RedisTestDatabeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(RedisTestDatabean databean){
			return Arrays.asList(new StringField(FieldKeys.data, databean.data));
		}
	}

	/** constructor **********************************************************/

	public RedisTestDatabean(){
		this(null, null);
	}

	public RedisTestDatabean(String id, String data){
		this.key = new RedisTestDatabeanKey(id);
		this.data = data;
	}

	/** databean *************************************************************/

	@Override
	public Class<RedisTestDatabeanKey> getKeyClass(){
		return RedisTestDatabeanKey.class;
	}

	@Override
	public RedisTestDatabeanKey getKey(){
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
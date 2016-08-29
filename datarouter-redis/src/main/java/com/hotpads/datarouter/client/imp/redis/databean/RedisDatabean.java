package com.hotpads.datarouter.client.imp.redis.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class RedisDatabean extends BaseDatabean<RedisDatabeanKey,RedisDatabean>{

	private RedisDatabeanKey key;
	private String data;

	/** column names *********************************************************/

	public static class FieldKeys{
		public static final StringFieldKey data = new StringFieldKey("data");
	}

	public static class RedisDatabeanFielder extends BaseDatabeanFielder<RedisDatabeanKey,RedisDatabean>{

		public RedisDatabeanFielder(){
			super(RedisDatabeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(RedisDatabean databean){
			return Arrays.asList(new StringField(FieldKeys.data, databean.data));
		}
	}

	/** constructor **********************************************************/

	public RedisDatabean(){
		this(null, null);
	}

	public RedisDatabean(String id, String data){
		this.key = new RedisDatabeanKey(id);
		this.data = data;
	}

	/** databean *************************************************************/

	@Override
	public Class<RedisDatabeanKey> getKeyClass(){
		return RedisDatabeanKey.class;
	}

	@Override
	public RedisDatabeanKey getKey(){
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
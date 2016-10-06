package com.hotpads.datarouter.client.imp.redis.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class RedisDatabeanKey extends BasePrimaryKey<RedisDatabeanKey>{

	private String id;

	public static class FieldKeys{
		public static final StringFieldKey id = new StringFieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.id, id));
	}

	public RedisDatabeanKey(){
	}

	public RedisDatabeanKey(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}
}
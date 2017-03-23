package com.hotpads.handler.user;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63FieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class DatarouterUserKey extends BasePrimaryKey<DatarouterUserKey>{

	private Long id;

	public static class FieldKeys{
		public static final UInt63FieldKey id = new UInt63FieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new UInt63Field(FieldKeys.id, id));
	}

	public DatarouterUserKey(){}

	public DatarouterUserKey(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setId(Long id){
		this.id = id;
	}

}

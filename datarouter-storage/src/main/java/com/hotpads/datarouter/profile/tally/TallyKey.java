package com.hotpads.datarouter.profile.tally;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class TallyKey extends BasePrimaryKey<TallyKey>{

	private String userToken;

	public static class FieldKeys{
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.userToken, userToken));
	}

	public TallyKey(){
	}

	public TallyKey(String userToken){
		this.userToken = userToken;
	}

	public String getUserToken(){
		return userToken;
	}
}
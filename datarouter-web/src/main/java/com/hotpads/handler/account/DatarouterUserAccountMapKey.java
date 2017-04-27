package com.hotpads.handler.account;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63FieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.handler.user.DatarouterUserKey;

public class DatarouterUserAccountMapKey extends BasePrimaryKey<DatarouterUserAccountMapKey>{

	private Long userId;
	private String accountName;

	public DatarouterUserAccountMapKey(){

	}

	public DatarouterUserAccountMapKey(Long userId, String accountName){
		this.userId = userId;
		this.accountName = accountName;
	}

	private static class FieldKeys{
		private static final UInt63FieldKey userId = new UInt63FieldKey("userId");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new UInt63Field(FieldKeys.userId, userId),
				new StringField(DatarouterAccountKey.FieldKeys.accountName, accountName));
	}

	public DatarouterUserKey getDatarouterUserKey(){
		return new DatarouterUserKey(userId);
	}

	public DatarouterAccountKey getDatarouterAccountKey(){
		return new DatarouterAccountKey(accountName);
	}

}

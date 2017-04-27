
package com.hotpads.handler.account;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class DatarouterAccountKey extends BasePrimaryKey<DatarouterAccountKey>{

	private String accountName;

	public DatarouterAccountKey(){
	}

	public DatarouterAccountKey(String accountName){
		this.accountName = accountName;
	}

	public static class FieldKeys{
		public static final StringFieldKey accountName = new StringFieldKey("accountName");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(FieldKeys.accountName, accountName));
	}

	public String getAccountName(){
		return accountName;
	}

}
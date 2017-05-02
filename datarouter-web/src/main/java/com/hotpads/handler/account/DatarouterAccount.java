package com.hotpads.handler.account;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class DatarouterAccount extends BaseDatabean<DatarouterAccountKey,DatarouterAccount>{

	private DatarouterAccountKey key;
	private String apiKey;

	private static class FieldKeys{
		private static final StringFieldKey apiKey = new StringFieldKey("apiKey");
	}

	public DatarouterAccount(){
		this.key = new DatarouterAccountKey();
	}

	public DatarouterAccount(String accountName){
		this.key = new DatarouterAccountKey(accountName);
	}

	public static class DatarouterAccountFielder extends BaseDatabeanFielder<DatarouterAccountKey,DatarouterAccount>{

		public DatarouterAccountFielder(){
			super(DatarouterAccountKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterAccount account){
			return Arrays.asList(
					new StringField(FieldKeys.apiKey, account.apiKey));
		}

	}

	@Override
	public Class<DatarouterAccountKey> getKeyClass(){
		return DatarouterAccountKey.class;
	}

	@Override
	public DatarouterAccountKey getKey(){
		return key;
	}

}

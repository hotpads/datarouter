package com.hotpads.handler.account;

import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;

public class DatarouterAccount extends BaseDatabean<DatarouterAccountKey,DatarouterAccount>{

	private DatarouterAccountKey key;

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
		public List<Field<?>> getNonKeyFields(DatarouterAccount databean){
			return Collections.emptyList();
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

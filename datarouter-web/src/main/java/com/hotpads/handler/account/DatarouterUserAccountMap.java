package com.hotpads.handler.account;

import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;

public class DatarouterUserAccountMap extends BaseDatabean<DatarouterUserAccountMapKey,DatarouterUserAccountMap>{

	private DatarouterUserAccountMapKey key;

	public DatarouterUserAccountMap(){
		this.key = new DatarouterUserAccountMapKey();
	}

	public DatarouterUserAccountMap(Long userId, String accountName){
		this.key = new DatarouterUserAccountMapKey(userId, accountName);
	}

	public static class DatarouterUserAccountMapFielder
	extends BaseDatabeanFielder<DatarouterUserAccountMapKey,DatarouterUserAccountMap>{

		public DatarouterUserAccountMapFielder(){
			super(DatarouterUserAccountMapKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterUserAccountMap databean){
			return Collections.emptyList();
		}

	}

	@Override
	public Class<DatarouterUserAccountMapKey> getKeyClass(){
		return DatarouterUserAccountMapKey.class;
	}

	@Override
	public DatarouterUserAccountMapKey getKey(){
		return key;
	}

}

package com.hotpads.datarouter.client.imp.sqs.encode;

import javax.inject.Singleton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class JsonSqsEncoder implements SqsEncoder{

	private Gson gson;
	
	public JsonSqsEncoder(){
		this.gson = new GsonBuilder().create();
	}
	
	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>String encode(D databean){
		return gson.toJson(databean);
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>D decode(String string, Class<D> databeanClass){
		return gson.fromJson(string, databeanClass);
	}

}

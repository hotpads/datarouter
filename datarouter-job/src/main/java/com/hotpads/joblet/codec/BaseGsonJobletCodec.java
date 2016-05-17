package com.hotpads.joblet.codec;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hotpads.joblet.JobletCodec;

public abstract class BaseGsonJobletCodec<P> implements JobletCodec<P>{

	private static final Gson gson = new Gson();

	private final Type paramsType;

	public BaseGsonJobletCodec(Class<P> paramsClass){
		this.paramsType = paramsClass;
	}

	public BaseGsonJobletCodec(TypeToken<P> paramsTypeToken){
		this.paramsType = paramsTypeToken.getType();
	}

	@Override
	public String marshallData(P params){
		return gson.toJson(params);
	}

	@Override
	public P unmarshallData(String encodedParams){
		return gson.fromJson(encodedParams, paramsType);
	}

}

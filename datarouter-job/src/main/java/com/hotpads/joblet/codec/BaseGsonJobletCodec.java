package com.hotpads.joblet.codec;

import com.google.gson.Gson;
import com.hotpads.joblet.JobletCodec;

public abstract class BaseGsonJobletCodec<P> implements JobletCodec<P>{

	private static final Gson gson = new Gson();

	private final Class<P> paramsClass;

	public BaseGsonJobletCodec(Class<P> paramsClass){
		this.paramsClass = paramsClass;
	}

	@Override
	public String marshallData(P params){
		return gson.toJson(params);
	}

	@Override
	public P unmarshallData(String encodedParams){
		return gson.fromJson(encodedParams, paramsClass);
	}

}

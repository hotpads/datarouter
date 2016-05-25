package com.hotpads.joblet.codec;

import com.hotpads.joblet.JobletCodec;

public class StringJobletCodec implements JobletCodec<String>{

	@Override
	public String marshallData(String params){
		return params;
	}

	@Override
	public String unmarshallData(String encodedParams) {
		return encodedParams;
	}

	@Override
	public int calculateNumItems(String params){
		return 1;
	}

}

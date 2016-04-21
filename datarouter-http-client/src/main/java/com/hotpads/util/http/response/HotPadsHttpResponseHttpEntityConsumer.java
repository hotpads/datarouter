package com.hotpads.util.http.response;

import java.io.InputStream;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;

public class HotPadsHttpResponseHttpEntityConsumer implements Consumer<HttpEntity>{
	InputStream inputStream;
	Exception capturedException;

	@Override
	public void accept(HttpEntity httpEntity){
		try{
			this.inputStream = httpEntity == null ? null : httpEntity.getContent();
		}catch(Exception e){
			this.capturedException = e;
		}
	}

	public InputStream getInputStream() throws Exception{
		if(capturedException != null) {
			throw capturedException;
		}
		return inputStream;
	}

}

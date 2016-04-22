package com.hotpads.util.http.response;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;

public class HttpEntityInputStreamAccessor implements Consumer<HttpEntity>{
	private InputStream inputStream;
	private IOException capturedException;

	@Override
	public void accept(HttpEntity httpEntity){
		try{
			this.inputStream = httpEntity == null ? null : httpEntity.getContent();
		}catch(IOException e){
			this.capturedException = e;
		}
	}

	public InputStream getInputStream() throws IOException{
		if(capturedException != null) {
			throw capturedException;
		}
		return inputStream;
	}

}

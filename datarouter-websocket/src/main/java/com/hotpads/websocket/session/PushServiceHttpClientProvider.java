package com.hotpads.websocket.session;

import javax.inject.Provider;

import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;

public class PushServiceHttpClientProvider implements Provider<HotPadsHttpClient>{

	@Override
	public HotPadsHttpClient get(){
		return new HotPadsHttpClientBuilder().build();//TODO secure it
	}

}

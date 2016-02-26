package com.hotpads.websocket.session;

import javax.inject.Provider;

import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.SignatureValidator;

public class PushServiceHttpClientProvider implements Provider<HotPadsHttpClient>{
	public static final String
			CIPHER_KEY = "JWCnjKR-ASvk+d=PcwX*Kf#M9Xx?kxZs$j5S-4ny",
			SALT = "MuG=d*SLJtjR=FADCURRRL4vg6uJKC8L4XrnfzM=",
			API_KEY = "2E^qQf@$Cg-h_MF@e!u9RyqET^MFqNCBd&w*mFx%";

	@Override
	public HotPadsHttpClient get(){
		return new HotPadsHttpClientBuilder()
				.setSignatureValidator(new SignatureValidator(SALT))
				.setCsrfValidator(new CsrfValidator(CIPHER_KEY))
				.setApiKeyPredicate(new DefaultApiKeyPredicate(API_KEY))
				.build();
	}

}

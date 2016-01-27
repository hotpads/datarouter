package com.hotpads.websocket.session;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.SignatureValidator;

public class WebSocketApiDispatcher extends BaseDispatcher{

	public static final String WEBSOCKET_COMMAND = "/websocketCommand";

	public WebSocketApiDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);

		handleDir(urlPrefix + WEBSOCKET_COMMAND)
				.withHandler(WebSocketApiHandler.class)
				.withApiKey(new DefaultApiKeyPredicate(PushServiceHttpClientProvider.API_KEY))
				.withCsrfToken(new CsrfValidator(PushServiceHttpClientProvider.CIPHER_KEY,
				PushServiceHttpClientProvider.CIPHER_IV))
				.withSignature(new SignatureValidator(PushServiceHttpClientProvider.SALT));
	}

}

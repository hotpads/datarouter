package com.hotpads.websocket.session;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.dispatcher.BaseDispatcher;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.DefaultCsrfValidator;
import com.hotpads.util.http.security.DefaultSignatureValidator;

public class WebSocketApiDispatcher extends BaseDispatcher{

	public static final String WEBSOCKET_COMMAND = "/websocketCommand";

	public WebSocketApiDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);

		handleDir(urlPrefix + WEBSOCKET_COMMAND)
				.withHandler(WebSocketApiHandler.class)
				.withApiKey(new DefaultApiKeyPredicate(PushServiceHttpClientProvider.API_KEY))
				.withCsrfToken(new DefaultCsrfValidator(PushServiceHttpClientProvider.CIPHER_KEY))
				.withSignature(new DefaultSignatureValidator(PushServiceHttpClientProvider.SALT));
		handleDir(urlPrefix + WebSocketToolHandler.PATH)
				.withHandler(WebSocketToolHandler.class);
	}

}

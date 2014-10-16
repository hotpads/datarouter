package com.hotpads.notification;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.client.HotPadsHttpRequest;
import com.hotpads.util.http.client.HotPadsHttpRequest.HttpMethod;
import com.hotpads.util.http.client.security.CsrfValidator;
import com.hotpads.util.http.client.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.client.security.SignatureValidator;

@Singleton
public class NotificationApiClient {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String CIPHER_KEY = "mcs,8<iBTizAAw<':m5{Mm3SSE&{LBGMFFA4e[*(";
	private static final String CIPHER_IV = "{YJ#]<^DF_65)Vr<kyrO*_.+U'>cl9/~7Naly_Kt";
	private static final String SALT = "5znm$#0D&~Z_B@]7<+;bVTM%XVbJ_iqzp]Vk[<J|";
	private static final String API_KEY = "W^m<-m80dcn+tb[M)EOWBG'+;K?y/2";

	private HotPadsHttpClient client;
	private NotificationRequestDtoTool dtoTool;
	private ExceptionHandlingConfig exceptionHandlingConfig;

	@Inject
	public NotificationApiClient(NotificationRequestDtoTool dtoTool, ExceptionHandlingConfig exceptionHandlingConfig) {
		this.exceptionHandlingConfig = exceptionHandlingConfig;
		this.dtoTool = dtoTool;
		this.client = new HotPadsHttpClientBuilder().create()
				.setSignatureValidator(new SignatureValidator(SALT))
				.setCsrfValidator(new CsrfValidator(CIPHER_KEY, CIPHER_IV))
				.setApiKeyPredicate(new DefaultApiKeyPredicate(API_KEY))
				.build();
	}

	public void call(List<Pair<NotificationRequest, ExceptionRecord>> requests) throws IOException {
		String url = exceptionHandlingConfig.getNotificationApiEndPoint();
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpMethod.POST, url, false);
		client.addDtosToPayload(request, dtoTool.toDtos(requests), null).execute(request);
	}

}

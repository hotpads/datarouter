package com.hotpads.notification;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.exception.HotPadsHttpException;
import com.hotpads.util.http.security.DefaultCsrfValidator;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.DefaultSignatureValidator;

@Singleton
public class NotificationApiClient{
	private static final String CIPHER_KEY = "mcs,8<iBTizAAw<':m5{Mm3SSE&{LBGMFFA4e[*(";
	private static final String SALT = "5znm$#0D&~Z_B@]7<+;bVTM%XVbJ_iqzp]Vk[<J|";
	private static final String API_KEY = "W^m<-m80dcn+tb[M)EOWBG'+;K?y/2";

	public static final String NOTIFICATION_API_CLIENT = "notificationApiClient";

	public static class NotificationApiClientHttpClientProvider implements Provider<HotPadsHttpClient>{
		@Override
		public HotPadsHttpClient get(){
			return new HotPadsHttpClientBuilder()
					.setSignatureValidator(new DefaultSignatureValidator(SALT))
					.setCsrfValidator(new DefaultCsrfValidator(CIPHER_KEY))
					.setApiKeyPredicate(new DefaultApiKeyPredicate(API_KEY))
					.build();
		}
	}

	private NotificationRequestDtoTool dtoTool;
	private ExceptionHandlingConfig exceptionHandlingConfig;
	private HotPadsHttpClient httpClient;

	@Inject
	public NotificationApiClient(NotificationRequestDtoTool dtoTool, ExceptionHandlingConfig exceptionHandlingConfig,
			@Named(NOTIFICATION_API_CLIENT) HotPadsHttpClient httpClient){
		this.dtoTool = dtoTool;
		this.exceptionHandlingConfig = exceptionHandlingConfig;
		this.httpClient = httpClient;
	}

	public void call(List<Pair<NotificationRequest,ExceptionRecord>> requests) throws HotPadsHttpException{
		String url = exceptionHandlingConfig.getNotificationApiEndPoint();
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, url, false);
		httpClient.addDtoToPayload(request, dtoTool.toDtos(requests), null).executeChecked(request);
	}

}

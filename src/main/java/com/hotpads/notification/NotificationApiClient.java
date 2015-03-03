package com.hotpads.notification;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.setting.DatarouterNotificationSettings;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.exception.HotPadsHttpException;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.SignatureValidator;

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
	private DatarouterNotificationSettings settings;
	private Boolean ignoreSsl;

	@Inject
	public NotificationApiClient(NotificationRequestDtoTool dtoTool, ExceptionHandlingConfig exceptionHandlingConfig,
			DatarouterNotificationSettings settings) {
		this.settings = settings;
		this.exceptionHandlingConfig = exceptionHandlingConfig;
		this.dtoTool = dtoTool;
	}

	public void call(List<Pair<NotificationRequest, ExceptionRecord>> requests) throws HotPadsHttpException {
		String url = exceptionHandlingConfig.getNotificationApiEndPoint();
		HotPadsHttpClient httpClient = getClient(settings.getIgnoreSsl().getValue());
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, url, false);
		httpClient.addDtoToPayload(request, dtoTool.toDtos(requests), null).executeChecked(request);
	}

	private HotPadsHttpClient getClient(Boolean ignoreSsl) {
		if (this.ignoreSsl == null || this.ignoreSsl != ignoreSsl) {
			 buildClient(ignoreSsl);
			 this.ignoreSsl = ignoreSsl;
		}
		return client;
	}

	private void buildClient(Boolean ignoreSsl) {
		client = new HotPadsHttpClientBuilder().setIgnoreSsl(ignoreSsl)
				.setSignatureValidator(new SignatureValidator(SALT))
				.setCsrfValidator(new CsrfValidator(CIPHER_KEY, CIPHER_IV))
				.setApiKeyPredicate(new DefaultApiKeyPredicate(API_KEY))
				.build();
	}

}

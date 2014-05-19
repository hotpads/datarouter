package com.hotpads.notification;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.setting.NotificationSettings;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.client.security.ApiKeyPredicate;
import com.hotpads.util.http.client.security.CsrfValidator;
import com.hotpads.util.http.client.security.SignatureValidator;

@Singleton
public class NotificationApiClient {

	private static final String CIPHER_KEY = "mcs,8<iBTizAAw<':m5{Mm3SSE&{LBGMFFA4e[*(";
	private static final String CIPHER_IV = "{YJ#]<^DF_65)Vr<kyrO*_.+U'>cl9/~7Naly_Kt";
	private static final String SALT = "5znm$#0D&~Z_B@]7<+;bVTM%XVbJ_iqzp]Vk[<J|";
	private static final String API_KEY = "W^m<-m80dcn+tb[M)EOWBG'+;K?y/2";

	private HotPadsHttpClient client;
	private NotificationRequestDtoTool dtoTool;
	private ExceptionHandlingConfig exceptionHandlingConfig;
	private NotificationSettings settings;
	private Boolean last;

	@Inject
	public NotificationApiClient(NotificationRequestDtoTool dtoTool, ExceptionHandlingConfig exceptionHandlingConfig, NotificationSettings settings) {
		this.settings = settings;
		this.exceptionHandlingConfig = exceptionHandlingConfig;
		this.dtoTool = dtoTool;
	}

	public void call(List<Pair<NotificationRequest, ExceptionRecord>> requests) throws IOException {
		getClient(settings.getIgnoreSsl().getValue()).post(exceptionHandlingConfig.getNotificationApiEndPoint(), dtoTool.toDtos(requests), false);
	}

	private HotPadsHttpClient getClient(Boolean ignoreSsl) {
		if (last == null || last != ignoreSsl) {
			 buildClient(ignoreSsl);
			 last = ignoreSsl;
		}
		return client;
	}

	private void buildClient(Boolean ignoreSsl) {
		HotPadsHttpClientBuilder httpClientBuilder = null;
		if (settings.getIgnoreSsl().getValue()) {
			try{
				SSLContextBuilder builder = new SSLContextBuilder();
				builder.loadTrustMaterial(null, new TrustStrategy(){
	
					@Override
					public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException{
						return true;
					}
	
				});
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(sslsf).build();
				httpClientBuilder = new HotPadsHttpClientBuilder().create().setCustomHttpClient(httpClient);
			}catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e){
				e.printStackTrace();
			}
		} else {
			httpClientBuilder = new HotPadsHttpClientBuilder().create();
		}
		client = httpClientBuilder
				.setSignatureValidator(new SignatureValidator(SALT))
				.setCsrfValidator(new CsrfValidator(CIPHER_KEY, CIPHER_IV))
				.setApiKeyPredicate(new ApiKeyPredicate(API_KEY))
				.build();
	}

}

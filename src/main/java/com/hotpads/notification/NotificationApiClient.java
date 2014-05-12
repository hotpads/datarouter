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
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.setting.NotificationSettings;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;

@Singleton
public class NotificationApiClient {

	private HotPadsHttpClient client;
	private NotificationRequestDtoTool dtoTool;
	private ExceptionHandlingConfig exceptionHandlingConfig;

	@Inject
	public NotificationApiClient(NotificationRequestDtoTool dtoTool, ExceptionHandlingConfig exceptionHandlingConfig, NotificationSettings settings) {
		CloseableHttpClient httpClient;
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
				httpClient = HttpClientBuilder.create().setSSLSocketFactory(sslsf).build();
				this.client = new HotPadsHttpClientBuilder().create().setCustomHttpClient(httpClient).build();
			}catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e){
				e.printStackTrace();
			}
		} else {
			this.client = new HotPadsHttpClientBuilder().createInstance();
		}
		this.exceptionHandlingConfig = exceptionHandlingConfig;
		this.dtoTool = dtoTool;
	}

	public void call(List<NotificationRequest> requests) throws IOException {
		client.post(exceptionHandlingConfig.getNotificationApiEndPoint(), dtoTool.toDtos(requests), false);
	}

}

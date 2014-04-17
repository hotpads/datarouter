package com.hotpads.handler.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.util.core.ListTool;

@Singleton
public class NotificationApiCaller {

	private static Logger logger = Logger.getLogger(NotificationApiCaller.class);

	private NotificationApiConfig notificationApiConfig;

	private HttpClient client;

	private boolean warmedup;

	@Inject
	public NotificationApiCaller(NotificationApiConfig notificationApiConfig) {
		this.notificationApiConfig = notificationApiConfig;
		client = new DefaultHttpClient();
	}

	public void setClient(HttpClient client) {
		this.client = client;
		warmedup = false;
		warmup();
	}

	public void warmup() {
		if (!warmedup) {
			HttpPost post = new HttpPost(notificationApiConfig.getEndPoint());
			try {
				HttpResponse response = client.execute(post);
				EntityUtils.consume(response.getEntity());
				warmedup = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void call(List<NotificationRequest> requests) throws IOException {
		HttpPost post = new HttpPost(notificationApiConfig.getEndPoint());
		List<NameValuePair> params = ListTool.create();

		JSONArray requestsJsonArray= new JSONArray();
		for (NotificationRequest request : requests) {
			JSONObject notification = new JSONObject();
			notification.put(notificationApiConfig.getUserTypeKey(), request.getKey().getUserType());
			notification.put(notificationApiConfig.getUserIdKey(), request.getKey().getUserId());
			notification.put(notificationApiConfig.getTypeKey(), request.getType());
			notification.put(notificationApiConfig.getDataKey(), request.getData());
			notification.put(notificationApiConfig.getChannelKey(), request.getChannel());
			requestsJsonArray.add(notification);
		}
		params.add(new BasicNameValuePair(notificationApiConfig.getParamName(), requestsJsonArray.toString()));
		post.setEntity(new UrlEncodedFormEntity(params));
		HttpResponse response = client.execute(post);

		if (response.getStatusLine().getStatusCode() != 200) {
			logger.error("Error calling notification API (status code " + response.getStatusLine().getStatusCode() + ")");
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = null;
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
			rd.close();
		}

		EntityUtils.consume(response.getEntity());
	}

}

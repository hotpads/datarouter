package com.hotpads.handler.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.inject.Inject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;

public class NotificationApiCaller {

	private static Logger logger = Logger.getLogger(NotificationApiCaller.class);
	
	@Inject
	private NotificationApiConfig notificationApiConfig;
	
	private HttpClient client;

	public NotificationApiCaller() {

	}

	public NotificationApiCaller(HttpClient client) {
		this.client = client;
	}

	public void call(String userType, String userId, Long time, String type, String data) throws IOException {
		if (ObjectTool.anyNull(client))
			client = new DefaultHttpClient();
		HttpPost post = new HttpPost(notificationApiConfig.getEndPoint());
		List<NameValuePair> params = ListTool.create();

		JSONArray requests = new JSONArray();
		JSONObject notification = new JSONObject();
		notification.put(notificationApiConfig.getUserTypeKey(), userType);
		notification.put(notificationApiConfig.getUserIdKey(), userId);
		notification.put(notificationApiConfig.getTimeKey(), time);
		notification.put(notificationApiConfig.getTypeKey(), type);
		notification.put(notificationApiConfig.getDataKey(), data);
		requests.add(notification);

		params.add(new BasicNameValuePair(notificationApiConfig.getParamName(), requests.toString()));
		post.setEntity(new UrlEncodedFormEntity(params));
		HttpResponse response = client.execute(post);

		if (response.getStatusLine().getStatusCode() != 200) {
			logger.error("Error calling notification API (status code " + response.getStatusLine().getStatusCode() + ")");
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = null;
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		}
	}
}

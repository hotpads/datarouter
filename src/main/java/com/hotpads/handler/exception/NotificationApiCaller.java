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

import com.hotpads.util.core.ListTool;

@Singleton
public class NotificationApiCaller {

	private static Logger logger = Logger.getLogger(NotificationApiCaller.class);
	
	private NotificationApiConfig notificationApiConfig;
	
	private HttpClient client;

	@Inject
	public NotificationApiCaller(NotificationApiConfig notificationApiConfig) {
		this.notificationApiConfig = notificationApiConfig;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public void call(String userType, String userId, String type, String data) throws IOException {
		if (client == null)
			client = new DefaultHttpClient();
		HttpPost post = new HttpPost(notificationApiConfig.getEndPoint());
		List<NameValuePair> params = ListTool.create();

		JSONArray requests = new JSONArray();
		JSONObject notification = new JSONObject();
		notification.put(notificationApiConfig.getUserTypeKey(), userType);
		notification.put(notificationApiConfig.getUserIdKey(), userId);
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
			rd.close();
		}
		
		EntityUtils.consume(response.getEntity());
	}

}

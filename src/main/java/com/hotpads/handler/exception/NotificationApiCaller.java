package com.hotpads.handler.exception;

import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_API_DATA;
import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_API_ENDPOINT;
import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_API_PARAM_NAME;
import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_API_TIME;
import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_API_TYPE;
import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_API_USER_ID;
import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_API_USER_TYPE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;

public class NotificationApiCaller {

	private HttpClient client;

	public NotificationApiCaller() {

	}

	public NotificationApiCaller(HttpClient client) {
		super();
		this.client = client;
	}

	public void call(String userType, String userId, Long time, String type, String data) throws IOException {
		if (ObjectTool.anyNull(client))
			client = new DefaultHttpClient();
		HttpPost post = new HttpPost(NOTIFICATION_API_ENDPOINT);
		List<NameValuePair> params = ListTool.create();

		JSONArray requests = new JSONArray();
		JSONObject notification = new JSONObject();
		notification.put(NOTIFICATION_API_USER_TYPE, userType);
		notification.put(NOTIFICATION_API_USER_ID, userId);
		notification.put(NOTIFICATION_API_TIME, time);
		notification.put(NOTIFICATION_API_TYPE, type);
		notification.put(NOTIFICATION_API_DATA, data);
		requests.add(notification);

		params.add(new BasicNameValuePair(NOTIFICATION_API_PARAM_NAME, requests.toString()));
		post.setEntity(new UrlEncodedFormEntity(params));
		HttpResponse response = client.execute(post);

		boolean debug = false;

		if (debug) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = null;
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		}
	}
}

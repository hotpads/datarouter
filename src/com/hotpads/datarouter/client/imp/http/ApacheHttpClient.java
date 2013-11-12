package com.hotpads.datarouter.client.imp.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.hotpads.util.core.io.ReaderTool;
import com.hotpads.util.core.java.ReflectionTool;


public class ApacheHttpClient{
	private static Logger logger = Logger.getLogger(ApacheHttpClient.class);

	private static final String AUTH_KEY = "11a682aac58c4e802c335228c2de5adc";
	private static final String AUTH_SECRET = "4baeb1f5b0740abb35afcee91941d11f";
	private static final String USERNAME = "datarouter";
	private static final String PASSWORD = "h19y6t643k8";
	private static final String PARAMS_SIGNATURE = "signature";
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	private static final int TOTAL_CONNECTIONS = 50;
	private static final int DEFAULT_MAX_CONNECTIONS = 50;
	private static final int SOCKET_TIMEOUT = 30000;
	private static final int CONNECTION_TIMEOUT = 5000;
	

	private String url;
	private HttpClient httpClient;
	

	public ApacheHttpClient(String url){
		this.url = url;
		this.httpClient = initHttpClient();
	}
	
	
	private HttpClient initHttpClient(){
		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setConnectionTimeout(CONNECTION_TIMEOUT);
		params.setSoTimeout(SOCKET_TIMEOUT);
		params.setDefaultMaxConnectionsPerHost(DEFAULT_MAX_CONNECTIONS);
		params.setMaxTotalConnections(TOTAL_CONNECTIONS);

		// retry connections 10 times.
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(10, true);
		params.setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);

		manager.setParams(params);
		HttpClient threadSafeHttpClient = new HttpClient(manager);

		// Basic HTTP Authentication.
		Credentials credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD);
		threadSafeHttpClient.getState().setCredentials(new AuthScope(url, 80, AuthScope.ANY_REALM), credentials);
		threadSafeHttpClient.getParams().setAuthenticationPreemptive(true);
		return threadSafeHttpClient;
	}
	


	public <T extends JSON> T request(Map<String,String> params, String uri, Class<T> returnType){
		String signature = generateSignature(uri, params, AUTH_SECRET);
		// Build parameters and create a POST request.
		StringBuilder parameters = new StringBuilder();
		for(Entry<String,String> entry : params.entrySet()){
			parameters.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
		}
		parameters.append(PARAMS_SIGNATURE).append('=').append(signature);
		StringRequestEntity entity = null;
		try{
			entity = new StringRequestEntity(parameters.toString(), "application/x-www-form-urlencoded", "UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
		PostMethod post = new PostMethod(uri);
		post.setRequestEntity(entity);
		try{
			httpClient.executeMethod(post);
			if(post.getStatusCode() == HttpStatus.SC_OK){
				Reader reader = new InputStreamReader(post.getResponseBodyAsStream(), post.getResponseCharSet());
				return getJson(reader, returnType);
			}else{
				logger.warn("Post request unsuccessful, returned HTTPStatus:" + post.getStatusCode()
						+ " for parameters:" + parameters.toString());
				return ReflectionTool.create(returnType);
			}
		}catch(ClientProtocolException e){
			throw new RuntimeException(e);
		}catch(JSONException e){
			throw new RuntimeException(e);
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			post.releaseConnection();
		}
	}

	private static <T extends JSON> T getJson(Reader reader, Class<T> returnType){
		String jsonString = ReaderTool.accumulateStringAndClose(reader).toString();
		if(!Strings.isNullOrEmpty(jsonString)){
			T json = (T) JSONSerializer.toJSON(jsonString);
			return json;
		}else{
			logger.error("Server returned empty/null json object: '" + jsonString + "'");
			return ReflectionTool.create(returnType);
		}
	}

	/**
	 * Generates a HMAC-SHA1 signature using the specified secretKey.
	 */
	private static String generateSignature(String endPoint, Map<String,String> params, String secretKey){
		StringBuilder result = new StringBuilder();
		try{
			for(Entry<String,String> entry : params.entrySet()){
				result.append(entry.getKey()).append('=').append(
						URLEncoder.encode(entry.getValue(), "UTF-8").replaceAll("\\+", "%20")).append('&');
			}
			String url = endPoint + '?' + result.deleteCharAt(result.length() - 1).toString();
			// get a hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), HMAC_SHA1_ALGORITHM);
			// get a hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			// compute hmac on input data bytes
			byte[] rawHmac = mac.doFinal(url.getBytes());
			// base64-encode hmac and trim off /r/n
			return Base64.encodeBase64String(rawHmac).trim();
		}catch(InvalidKeyException e){
			throw new IllegalArgumentException(e);
		}catch(NoSuchAlgorithmException e){
			throw new IllegalArgumentException(e);
		}catch(UnsupportedEncodingException e){
			throw new IllegalArgumentException(e);
		}
	}


	public String getAuthKey(){
		return AUTH_KEY;
	}


	public String getAuthSecret(){
		return AUTH_SECRET;
	}


	public HttpClient getHttpClient(){
		return httpClient;
	}
}

package com.hotpads.util.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import com.hotpads.util.http.client.json.JsonSerializer;
import com.hotpads.util.http.client.security.ApiKeyPredicate;
import com.hotpads.util.http.client.security.CsrfValidator;
import com.hotpads.util.http.client.security.SecurityParameters;
import com.hotpads.util.http.client.security.SignatureValidator;

@Singleton
public class HotPadsHttpClient{

	private HttpClient httpClient;
	private JsonSerializer jsonSerializer;
	private HotPadsRetryHandler retryHandler;
	private SignatureValidator signatureValidator;
	private CsrfValidator csrfValidator;
	private ApiKeyPredicate apiKeyPredicate;
	
	HotPadsHttpClient(HttpClient httpClient, JsonSerializer jsonSerializer, SignatureValidator signatureValidator, CsrfValidator csrfValidator, ApiKeyPredicate apiKeyPredicate){
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureValidator = signatureValidator;
		this.csrfValidator = csrfValidator;
		this.apiKeyPredicate = apiKeyPredicate;
	}
	
	/**** HTTP GET request methods *****/

	public String get(String url, boolean retrySafe){
		HttpGet request = new HttpGet(url);
		return execute(request, retrySafe);
	}
	
	public <E> E get(String url, Class<E> classOfDataTranferObjectExpected, boolean retrySafe){
		return jsonSerializer.deserialize(get(url, retrySafe), classOfDataTranferObjectExpected);
	}
	
	/**** HTTP POST request methods *****/
	
	public String post(String url, Map<String, String> data, boolean retrySafe){
		HttpPost request = new HttpPost(url);
		if(csrfValidator != null)
			data.put(SecurityParameters.CSRF_TOKEN,  csrfValidator.generateCsrfToken());
		if(apiKeyPredicate != null)
			data.put(SecurityParameters.API_KEY, apiKeyPredicate.getApiKey());
		if(signatureValidator != null)
			data.put(SecurityParameters.SIGNATURE, Base64.encodeBase64String(signatureValidator.sign(data)));
		try{
			request.setEntity(new UrlEncodedFormEntity(urlEncodeFromMap(data)));
		}catch (UnsupportedEncodingException e){
			throw new HotPadsHttpClientException(e);
		}
		return execute(request, retrySafe);
	}

	public <T> String post(String url, T dataTransferObjectToPost, boolean retrySafe){
		Map<String, String> params = new HashMap<String, String>();
		params.put("dataTransferObject", jsonSerializer.serialize(dataTransferObjectToPost));
		return post(url, params, retrySafe);
	}
	
	public <T,E> E post(String url, T dataTransferObjectToPost, Class<E> classOfDataTranferObjectExpected, boolean retrySafe){
		return jsonSerializer.deserialize(post(url, dataTransferObjectToPost, retrySafe), classOfDataTranferObjectExpected);
	}
	
	/***** private ******/
	
	private String execute(HttpUriRequest request, boolean retrySafe){
		HttpResponse response;
		String responseString = "";
		retryHandler.setRetrySafe(request);
		try{
			response = httpClient.execute(request);
			if(response.getStatusLine().getStatusCode() != 200){
				throw new HotPadsHttpClientException(response);
			}
			responseString = streamToString(response.getEntity().getContent());
		}catch (IOException e){
			throw new HotPadsHttpClientException(e);
		} finally{
			retryHandler.clean(request);
		}
		return responseString;
	}

	private String streamToString(InputStream input){
		try(Scanner s = new Scanner(input)){
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
	
	private List<NameValuePair> urlEncodeFromMap(Map<String, String> data){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for(Entry<String, String> entry : data.entrySet()){
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		return params;
	}
	
	void setRetryHandler(HotPadsRetryHandler retryHandler){
		this.retryHandler = retryHandler;
	}
}

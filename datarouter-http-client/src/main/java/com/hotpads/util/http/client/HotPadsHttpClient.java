package com.hotpads.util.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.hotpads.util.http.client.json.JsonSerializer;
import com.hotpads.util.http.client.security.ApiKeyPredicate;
import com.hotpads.util.http.client.security.CsrfValidator;
import com.hotpads.util.http.client.security.SecurityParameters;
import com.hotpads.util.http.client.security.SignatureValidator;

@Singleton
public class HotPadsHttpClient{

	private static final String CONTENT_TYPE = "Content-Type";
	
	private HttpClient httpClient;
	private JsonSerializer jsonSerializer;
	private SignatureValidator signatureValidator;
	private CsrfValidator csrfValidator;
	private ApiKeyPredicate apiKeyPredicate;
	private HotPadsHttpClientConfig config;
	
	HotPadsHttpClient(HttpClient httpClient, JsonSerializer jsonSerializer, SignatureValidator signatureValidator, 
			CsrfValidator csrfValidator, ApiKeyPredicate apiKeyPredicate, HotPadsHttpClientConfig config){
		this.httpClient = httpClient;
		this.jsonSerializer = jsonSerializer;
		this.signatureValidator = signatureValidator;
		this.csrfValidator = csrfValidator;
		this.apiKeyPredicate = apiKeyPredicate;
		this.config = config;
	}
	
	/**** HTTP GET request methods *****/

	public String get(String url, boolean retrySafe, Map<String, String> headers){
		HttpGet request = new HttpGet(url);
		return execute(request, retrySafe, headers);
	}
	
	public String get(String url, boolean retrySafe){
		return get(url, retrySafe, new HashMap<String, String>());
	}
	
	public <E> E get(String url, Type classOfDataTranferObjectExpected, boolean retrySafe, Map<String, String> headers){
		return jsonSerializer.deserialize(get(url, retrySafe, headers), classOfDataTranferObjectExpected);
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
		return execute(request, retrySafe, null);
	}
	
	public String post(String url, String stringEntity, boolean retrySafe, String contentType,
			Map<String, String> headers){
		HttpPost request = new HttpPost(url);
		try{
			request.setEntity(new StringEntity(stringEntity));
		}catch (UnsupportedEncodingException e){
			throw new HotPadsHttpClientException(e);
		}
		if(contentType!=null){
			request.setHeader(CONTENT_TYPE, contentType);
		}
		return execute(request, retrySafe, headers);
	}
	
	public String post(String url, String stringEntity, boolean retrySafe, String contentType){
		return post(url, stringEntity, retrySafe, contentType, null);
	}
	
	public <E> E post(String url, Map<String, String> data, Class<E> typeOfE, boolean retrySafe){
		return jsonSerializer.deserialize(post(url, data, retrySafe), typeOfE);
	}

	public <T> String post(String url, T dataTransferObjectToPost, boolean retrySafe){
		return post(url, dataTransferObjectToPost, retrySafe, null);
	}
	public <T> String post(String url, T dataTransferObjectToPost, boolean retrySafe, String dataTransferObjectType){
		String serializedDto = jsonSerializer.serialize(dataTransferObjectToPost);
		String dtoType = dataTransferObjectType;
		if(dtoType == null){
			dtoType = dataTransferObjectToPost.getClass().getCanonicalName();
		}
		return post(url, serializedDto, dtoType, retrySafe);
	}
	
	public <T> String post(String url, Collection<T> dtoCollection, boolean retrySafe){
		return post(url, dtoCollection, retrySafe, null);
	}
	public <T> String post(String url, Collection<T> dtoCollection, boolean retrySafe, String dataTransferObjectType){
		String serializedDtos = jsonSerializer.serialize(dtoCollection);
		String dtoType = dataTransferObjectType;
		if(dtoType == null){
			if(dtoCollection.isEmpty()){
				dtoType = "";
			} else{
				dtoType = dtoCollection.iterator().next().getClass().getCanonicalName();
			}
		}
		return post(url, serializedDtos, dtoType, retrySafe);
	}
	
	private String post(String url, String dto, String dtoType, boolean retrySafe){
		Map<String, String> params = new HashMap<String, String>();
		params.put(config.getDtoParameterName(), dto);
		params.put(config.getDtoTypeParameterName(), dtoType);
		return post(url, params, retrySafe);
	}
	
	public <T,E> E post(String url, T dataTransferObjectToPost, Type typeOfDataTranferObjectExpected,
			boolean retrySafe){
		return post(url, dataTransferObjectToPost, null, typeOfDataTranferObjectExpected, retrySafe);
	}
	
	public <T,E> E post(String url, T dataTransferObjectToPost, String dataTransferObjectToPostType,
			Type typeOfDataTranferObjectExpected, boolean retrySafe){
		return jsonSerializer.deserialize(post(url, dataTransferObjectToPost, retrySafe, dataTransferObjectToPostType),
				typeOfDataTranferObjectExpected);
	}
	
	/*** PATCH ***/
	
	public void patch(String url, String serializedObject, boolean retrySafe, Map<String, String> headers){
		HttpPatch request = new HttpPatch(url);
		try{
			request.setEntity(new StringEntity(serializedObject));
		}catch (UnsupportedEncodingException e){
			throw new HotPadsHttpClientException(e);
		}
		request.setHeader(CONTENT_TYPE, "application/json");
		execute(request, retrySafe, headers);
	}
	
	public <T> void patch(String url, T object, boolean retrySafe, Map<String, String> headers){
		patch(url, jsonSerializer.serialize(object), retrySafe, headers);
	}
	
	/***** private ******/
	
	private String execute(HttpUriRequest request, Boolean retrySafe, Map<String, String> headers){
		HttpResponse response;
		String responseString = "";
		setHeaders(request, headers);
		HttpContext context = new BasicHttpContext();
		context.setAttribute(HotPadsRetryHandler.RETRY_SAFE_ATTRIBUTE, retrySafe);
		try{
			response = httpClient.execute(request, context);
			if(response.getStatusLine().getStatusCode() > 300){
				HotPadsHttpResponse hpResponse = new HotPadsHttpResponse(response);
				throw new HotPadsHttpClientException(hpResponse);
			}
			if(response.getEntity() == null){
				return "";
			}
			responseString = streamToString(response.getEntity().getContent());
		}catch (IOException e){
			throw new HotPadsHttpClientException(e);
		}
		return responseString;
	}
	
	public static void setHeaders(HttpUriRequest request, Map<String, String> headers){
		if(headers == null){
			return;
		}
		for(Entry<String, String> header : headers.entrySet()){
			request.addHeader(header.getKey(), header.getValue());
		}
	}

	private static String streamToString(InputStream input){
		try(Scanner s = new Scanner(input)){
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
	
	private static List<NameValuePair> urlEncodeFromMap(Map<String, String> data){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for(Entry<String, String> entry : data.entrySet()){
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		return params;
	}

}

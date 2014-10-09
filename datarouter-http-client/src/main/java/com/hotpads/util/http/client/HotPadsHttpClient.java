package com.hotpads.util.http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;

import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.hotpads.util.http.client.json.JsonSerializer;
import com.hotpads.util.http.client.security.ApiKeyPredicate;
import com.hotpads.util.http.client.security.CsrfValidator;
import com.hotpads.util.http.client.security.SecurityParameters;
import com.hotpads.util.http.client.security.SignatureValidator;

@Singleton
public class HotPadsHttpClient {
	
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

	public <T> String serialize(T object) {
		return jsonSerializer.serialize(object);
	}
	
	public <E> E deserialize(String dtoJson, Type typeOfE) {
		return jsonSerializer.deserialize(dtoJson, typeOfE);
	}
	
	public HotPadsHttpResponse executeRequest(HotPadsHttpRequest request) {
		HttpUriRequest httpRequest = request.getRequest();
		if(httpRequest instanceof HttpPost) {
			Map<String,String> payload = request.getPayload();
			if (csrfValidator != null) {
				payload.put(SecurityParameters.CSRF_TOKEN, csrfValidator.generateCsrfToken());
			}
			if (apiKeyPredicate != null) {
				payload.put(SecurityParameters.API_KEY, apiKeyPredicate.getApiKey());
			}
			if (signatureValidator != null) {
				payload.put(SecurityParameters.SIGNATURE, Base64.encodeBase64String(signatureValidator.sign(payload)));
			}
			request.setPayload(payload);
		}
		
		HttpContext context = new BasicHttpContext();
		context.setAttribute(HotPadsRetryHandler.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());
		HotPadsHttpResponse hpResponse;
		try {
			HttpResponse response = httpClient.execute(httpRequest, context);
			hpResponse = new HotPadsHttpResponse(response);
			if(response.getStatusLine().getStatusCode() > 300){
				throw new HotPadsHttpClientException(hpResponse);
			}
		} catch (IOException e) {
			throw new HotPadsHttpClientException(e);
		}
		
		return hpResponse;
	}
	
	public String executeToString(HotPadsHttpRequest request) {
		return entityString(executeRequest(request));
	}
	
	private String entityString(HotPadsHttpResponse response) {
		if (response == null || response.getEntity() == null) {
			return "";
		}
		try (Scanner s = new Scanner(response.getEntity().getContent())) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} catch (IOException e) {
			throw new HotPadsHttpClientException(e);
		}
	}
	
	public <T> HotPadsHttpRequest addDtosToPayload(HotPadsHttpRequest request, Collection<T> dtos, String dtoType) {
		String serializedDtos = jsonSerializer.serialize(dtos);
		String dtoTypeNullSafe = dtoType;
		if(dtoType == null) {
			dtoTypeNullSafe = dtos.isEmpty() ? "" : dtos.iterator().next().getClass().getCanonicalName();
		}
		Map<String,String> payload = request.getPayload();
		payload.put(config.getDtoParameterName(), serializedDtos);
		payload.put(config.getDtoTypeParameterName(), dtoTypeNullSafe);
		request.setPayload(payload);
		
		return request;
	}
//	
//	public <E> E post(String url, Map<String, String> data, Class<E> typeOfE, boolean retrySafe){
//		return jsonSerializer.deserialize(post(url, retrySafe, null, data), typeOfE);
//	}
//
//	public <T> String post(String url, T dtoToPost, boolean retrySafe, String dtoType){
//		String serializedDto = jsonSerializer.serialize(dtoToPost);
//		String dtoTypeNullSafe = dtoType == null ? dtoToPost.getClass().getCanonicalName() : dtoType;
//		return postDto(url, serializedDto, dtoTypeNullSafe, retrySafe);
//	}
//	
//	public <T> String post(String url, Collection<T> dtoCollection, boolean retrySafe, String dataTransferObjectType){
//		String serializedDtos = jsonSerializer.serialize(dtoCollection);
//		String dtoType = dataTransferObjectType;
//		if(dtoType == null){
//			dtoType = dtoCollection.isEmpty() ? "" : dtoCollection.iterator().next().getClass().getCanonicalName();
//		}
//		return postDto(url, serializedDtos, dtoType, retrySafe);
//	}
//	
//	private String postDto(String url, String dto, String dtoType, boolean retrySafe){
//		Map<String, String> params = new HashMap<String, String>();
//		params.put(config.getDtoParameterName(), dto);
//		params.put(config.getDtoTypeParameterName(), dtoType);
//		return post(url, retrySafe, null, params);
//	}
//	
//	public <T,E> E post(String url, T dataTransferObjectToPost, String dataTransferObjectToPostType,
//			Type typeOfDataTranferObjectExpected, boolean retrySafe){
//		String response = post(url, dataTransferObjectToPost, retrySafe, dataTransferObjectToPostType);
//		return jsonSerializer.deserialize(response, typeOfDataTranferObjectExpected);
//	}
//	
//	/*** PATCH ***/
//	
//	public void patch(String url, String serializedObject, boolean retrySafe, Map<String, String> headers){
//		headers.put(CONTENT_TYPE, "application/json");
//		HotPadsHttpRequest requestHp = new HotPadsHttpRequest(HttpMethod.PATCH, url);
//		requestHp.setRetrySafe(retrySafe);
//		try {
//			requestHp.setEntity(new StringEntity(serializedObject));
//		} catch (UnsupportedEncodingException e) {
//			throw new HotPadsHttpClientException(e);
//		}
//		requestHp.setHeaders(headers);
//		executeRequest(requestHp);
//	}
//	
//	public <T> void patch(String url, T object, boolean retrySafe, Map<String, String> headers){
//		patch(url, jsonSerializer.serialize(object), retrySafe, headers);
//	}
}

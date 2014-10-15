package com.hotpads.util.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;

import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
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
	
	public String execute(HotPadsHttpRequest request) {
		if(request.getRequest() instanceof HttpPost) {
			if (csrfValidator != null) {
				request.addToPayload(SecurityParameters.CSRF_TOKEN, csrfValidator.generateCsrfToken());
			}
			if (apiKeyPredicate != null) {
				request.addToPayload(SecurityParameters.API_KEY, apiKeyPredicate.getApiKey());
			}
			if (signatureValidator != null) {
				byte[] signature = signatureValidator.sign(request.getPayload());
				request.addToPayload(SecurityParameters.SIGNATURE, Base64.encodeBase64String(signature));
			}
		}
		
		HttpContext context = new BasicHttpContext();
		context.setAttribute(HotPadsRetryHandler.RETRY_SAFE_ATTRIBUTE, request.getRetrySafe());
		for(Map.Entry<String, String> param : request.getPayload().entrySet()) {
			context.setAttribute(param.getKey(), param.getValue());
		}
		
		HotPadsHttpResponse hpResponse;
		try {
			HttpResponse response = httpClient.execute(request.getRequest(), context);
			hpResponse = new HotPadsHttpResponse(response);
			if(response.getStatusLine().getStatusCode() > 300){
				throw new HotPadsHttpClientException(hpResponse);
			}
			return streamToString(response.getEntity().getContent());
		} catch (IOException e) {
			throw new HotPadsHttpClientException(e);
		}
	}
	
	public <E> E executeDeserialize(HotPadsHttpRequest request, Type typeOfE) {
		return deserialize(execute(request), typeOfE);
	}
	
	// TODO move method in HotPadsHttpRequest - problem comes in with the dependency on jsonSerializer and config
	public <T> HotPadsHttpClient addDtosToPayload(HotPadsHttpRequest request, Collection<T> dtos, String dtoType) {
		String serializedDtos = jsonSerializer.serialize(dtos);
		String dtoTypeNullSafe = dtoType;
		if(dtoType == null) {
			dtoTypeNullSafe = dtos.isEmpty() ? "" : dtos.iterator().next().getClass().getCanonicalName();
		}
		request.addToPayload(config.getDtoParameterName(), serializedDtos);
		request.addToPayload(config.getDtoTypeParameterName(), dtoTypeNullSafe);
		
		return this;
	}

	private String streamToString(InputStream input) {
		try (Scanner s = new Scanner(input)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
}

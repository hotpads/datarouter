package com.hotpads.salesforce;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.hotpads.salesforce.databean.SalesforceDatabean;
import com.hotpads.salesforce.databean.SalesforceDatabeanKey;
import com.hotpads.salesforce.dto.SalesforceAuthenticationResponse;
import com.hotpads.salesforce.dto.SalesforceQueryResult;
import com.hotpads.setting.DatarouterSalesforceSettings;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.client.HotPadsHttpClientException;

@Singleton
public class SalesforceApi{ //TODO make a datarouter Client and a Node
	
	private static final Logger logger = Logger.getLogger(SalesforceApi.class);

	private static final Long tokenLifetime = 600L; //Can be set on Salesforce but TODO auto reconnect
	private static final String ENDPOINT = "/services/data/v20.0/";
	private static final String SOBJECTS_ENDPOINT = ENDPOINT + "sobjects/";
	private static final String QUERY_ENDPOINT = ENDPOINT + "query/";
	
	private HotPadsHttpClient httpClient;
	private Long accessTokenCreationTime;
	private String accessToken;
	private String instanceUrl;
	private DatarouterSalesforceSettings settings;
	Map<Class<? extends SalesforceDatabean>, List<String>> authorizedFields;
	
	@Inject
	public SalesforceApi(DatarouterSalesforceSettings settings){
		this.settings = settings;
		this.authorizedFields = new HashMap<>();
		register(Featured_Property__c.class);
		this.httpClient = new HotPadsHttpClientBuilder()
		.setJsonSerializer(new SalesforceJsonSerializer(authorizedFields))
		.createInstance();
		this.connect();
	}
	
	//TODO in node
	public <D extends SalesforceDatabean> void register(Class<D> databeanClass){
		authorizedFields.put(databeanClass, ReflectionTool.create(databeanClass).getAuthorizedFields());
	}

	//TODO wrap into Op
	public <D extends SalesforceDatabean> D get(SalesforceDatabeanKey key, Class<D> databeanClass){
		try{
			String url = instanceUrl + SOBJECTS_ENDPOINT + databeanClass.getSimpleName() + "/" + key.getId();
			@SuppressWarnings("unchecked")
			D databean = (D) httpClient.get(url, databeanClass, true, getAuthenticationHeaders());
			databean.setKey(key);
			return databean;
		}catch(Exception e){
			logger.error("Error while retrieving " + databeanClass.getSimpleName() + "." + key.getId(), e);
			return null;
		}
	}
	
	public <D extends SalesforceDatabean> void put(D databean){
		put(databean, null);
	}
	
	//TODO wrap into Op
	public <D extends SalesforceDatabean> void put(D databean, List<String> overrideAuthorizedFields){
		String url = instanceUrl + SOBJECTS_ENDPOINT + databean.getClass().getSimpleName() + "/"
				+ databean.getKey().getId();
		if(overrideAuthorizedFields == null){
			httpClient.patch(url, databean, false, getAuthenticationHeaders());
		}else{
			Map<Class<? extends SalesforceDatabean>, List<String>> customAuthorizedFields = new HashMap<>();
			customAuthorizedFields.put(databean.getClass(), overrideAuthorizedFields);
			SalesforceJsonSerializer customSerializer = new SalesforceJsonSerializer(customAuthorizedFields);
			String serializedDatabean = customSerializer.serialize(databean);
			httpClient.patch(url, serializedDatabean, false, getAuthenticationHeaders());
		}
	}
	
	//TODO wrap into Op
	//TODO use BaseLookup
	public <D extends SalesforceDatabean> List<D> lookup(Class<D> databeanClass, String field, String value){
		StringBuilder urlBuilder = new StringBuilder(instanceUrl + QUERY_ENDPOINT + "?q=SELECT+");
		Field[] declaredFields = databeanClass.getDeclaredFields(); //TODO Use BaseDatabeanFielder
		for(int i = 0 ; i < declaredFields.length - 1 ; i++){
			urlBuilder.append(declaredFields[i].getName() + ",");
		}
		urlBuilder.append(declaredFields[declaredFields.length-1].getName());
		urlBuilder.append("+from+" + databeanClass.getSimpleName() + "+where+" + field + "='" + value + "'");
		String url = urlBuilder.toString();
		try{
			Type type = ReflectionTool.create(databeanClass).getQueryResultType();
			@SuppressWarnings("unchecked")
			SalesforceQueryResult<D> result = (SalesforceQueryResult<D>) httpClient.get(url, type, true, getAuthenticationHeaders());
			for(D record : result.records){
				record.setKey(new SalesforceDatabeanKey(StringTool.getStringAfterLastOccurrence("/",
						record.getAttributes().url)));
			}
			return result.records;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private void connect(){
		try{
			SalesforceAuthenticationResponse r = httpClient.post(
				settings.getLoginEndpoint().get(),
				getCredentials(),
				SalesforceAuthenticationResponse.class,
				true);
			accessTokenCreationTime = System.currentTimeMillis();
			accessToken = r.access_token;
			instanceUrl = r.instance_url;
		}catch(HotPadsHttpClientException e){
			logger.error("Could not connect to Salesforce API : " + e.getEntity());
		}
	}
	
	private Map<String, String> getAuthenticationHeaders(){
		refreshToken();
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + accessToken);
		return headers;
	}
	
	private void refreshToken(){
		boolean reconnect = accessTokenCreationTime == null || 
				System.currentTimeMillis() - accessTokenCreationTime > tokenLifetime;
				
		if(reconnect){
			connect();
		}
	}
	
	private Map<String, String> getCredentials(){
		Map<String, String> authParams = new HashMap<>();
		authParams.put("grant_type", "password");
		authParams.put("client_id", settings.getClientId().get());
		authParams.put("client_secret", settings.getClientSecret().get());
		authParams.put("username", settings.getUsername().get());
		authParams.put("password", settings.getPassword().get());
		return authParams;
	}
	
}

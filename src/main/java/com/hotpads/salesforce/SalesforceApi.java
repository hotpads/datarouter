package com.hotpads.salesforce;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.client.HotPadsHttpClientException;

public class SalesforceApi{
	
	private static final String ENDPOINT = "/services/data/v20.0/";
	private static final String SOBJECTS_ENDPOINT = ENDPOINT + "sobjects/";
	private static final String QUERY_ENDPOINT = ENDPOINT + "query/";
	private static final Logger logger = Logger.getLogger(SalesforceApi.class);
	
	private HotPadsHttpClient httpClient;
	private String accessToken;
	private String instanceUrl;
	
	public SalesforceApi(){
		this.httpClient = new HotPadsHttpClientBuilder().createInstance();
		this.connect();
	}

	private void connect(){
		Map<String, String> auth_params = new HashMap<>();
		auth_params.put("grant_type", "password");
		auth_params.put("client_id", "3MVG9zJJ_hX_0bb8Ps0FJvyGeLt9sR0rw5rWRpZlUnz4wg1nMbgo55G3rBFU8EIZkG0z3sq0j49p13yRwFbhT");
		auth_params.put("client_secret", "7796326880581196854");
		auth_params.put("username", "calixteb@zillow.com.rentalsdev");
		auth_params.put("password", "cbonsart03ncE8BuWJi7ApZl9uqv1ygpo9");
		try{
			AuthenticationResponse r = httpClient.post(
				"https://test.salesforce.com/services/oauth2/token",
				auth_params,
				AuthenticationResponse.class,
				true);
			accessToken = r.access_token;
			instanceUrl = r.instance_url;
		}catch(HotPadsHttpClientException e){
			logger.error("Could not connect to Salesforce API : " + e.getEntity());
		}
	}
	
	public <D extends SalesforceDatabean> D get(SalesforceDatabeanKey key, Class<D> databeanClass){
		String url = instanceUrl + SOBJECTS_ENDPOINT;
		url += databeanClass.getSimpleName();
		url += "/";
		url += key.getId();
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + accessToken);
		try{
			D databean = (D) httpClient.get(url, databeanClass, true, headers);
			databean.setKey(key);
			return databean;
		}catch(Exception e){
			return null;
		}
	}
	
	public <D extends SalesforceDatabean> void put(D databean){
		String url = instanceUrl + SOBJECTS_ENDPOINT;
		url += databean.getClass().getSimpleName();
		url += "/";
		url += databean.getKey().getId();
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + accessToken);
		httpClient.patch(url, databean, false, headers);
	}
	
	public <D extends SalesforceDatabean> List<D> lookup(Class<D> databeanClass, String field, String value){
		String url = instanceUrl + QUERY_ENDPOINT + "?q=SELECT+";
		for(Field fieldToSelect : databeanClass.getDeclaredFields()){
			url+= fieldToSelect.getName() + ",";
		}
		url = url.substring(0,  url.length()-1);
		url += "+from+" + databeanClass.getSimpleName() + "+where+" + field + "='" + value + "'";
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + accessToken);
		try{
			Type type = ReflectionTool.create(databeanClass).getQueryResultType();
			QueryResult<D> result = (QueryResult<D>) httpClient.get(url, type, true, headers);
			for(D record : result.records){
				record.setKey(new SalesforceDatabeanKey(StringTool.getStringAfterLastOccurrence("/", record.getAttributes().url)));
			}
			return result.records;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private static class QueryResult<D extends SalesforceDatabean>{
		private int totalSize;
		private boolean done;
		private List<D> records;
	}
	
	private static class Contact extends SalesforceDatabean{
		
		public Contact(){
			super(new SalesforceDatabeanKey(""));
		}
		
		public Contact(SalesforceDatabeanKey key){
			super(key);
		}

		private String FirstName;

		@Override
		public Type getQueryResultType(){
			return new TypeToken<QueryResult<Contact>>(){}.getType();
		}
		
	}
	
	private static class Attributes{
		private String url;
	}
	
	private static abstract class SalesforceDatabean{
		
		private transient SalesforceDatabeanKey key;
		private Attributes attributes;
		
		public SalesforceDatabean(SalesforceDatabeanKey key){
			this.key = key;
		}
		
		public SalesforceDatabeanKey getKey(){
			return key;
		}
		
		public void setKey(SalesforceDatabeanKey key){
			this.key = key;
		}
		
		public abstract Type getQueryResultType();

		public Attributes getAttributes(){
			return attributes;
		}

		public void setAttributes(Attributes attributes){
			this.attributes = attributes;
		}
		
	}
	
	private static class SalesforceDatabeanKey{
		private String id;

		public SalesforceDatabeanKey(String id){
			this.id = id;
		}

		public String getId(){
			return id;
		}

	}
	
	private static class AuthenticationResponse{
		public String access_token;
		public String instance_url;
	}
	
	private static class Featured_Property__c extends SalesforceDatabean{
		
		private String Name;
		private String Property_Zillow_Account_ID__c;

		public Featured_Property__c(){
			super(new SalesforceDatabeanKey(""));
		}
		
		public Featured_Property__c(SalesforceDatabeanKey key){
			super(key);
		}

		@Override
		public Type getQueryResultType(){
			return new TypeToken<QueryResult<Featured_Property__c>>(){}.getType();
		}
		
	}
	
	public static void main(String[] args){
		SalesforceApi salesforceApi = new SalesforceApi();

		List<Featured_Property__c> ff = salesforceApi.lookup(Featured_Property__c.class, "Property_Zillow_Account_ID__c", "ZRN-2327749");
		for(Featured_Property__c f : ff){
			System.out.println(f.Name);
		}
	}
	
}

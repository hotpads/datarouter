package com.hotpads.salesforce;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.client.HotPadsHttpClientException;

public class SalesforceApi{
	
	private static final String ENDPOINT = "/services/data/v20.0/sobjects/";
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
//		auth_params.put("grant_type", "password");
//		auth_params.put("client_id", "3MVG9zJJ_hX_0bb8Ps0FJvyGeLt9sR0rw5rWRpZlUnz4wg1nMbgo55G3rBFU8EIZkG0z3sq0j49p13yRwFbhT");
//		auth_params.put("client_secret", "7796326880581196854");
//		auth_params.put("username", "calixteb@zillow.com.rentalsdev");
//		auth_params.put("password", "3ub!&/I9dTCK");
		auth_params.put("grant_type", "password");
		auth_params.put("client_id", "3MVG99qusVZJwhskwH1RzuhefvEBvwF4lGwBlvQqCvlsJwMpKrtyvFDe5_3d_UG30hVcFljIdY5tVuwdmHpK3");
		auth_params.put("client_secret", "2182282072562754075");
		auth_params.put("username", "devteam@emocial.co.uk");
		auth_params.put("password", "emocial2014yKIftL6FqKDeDY9rVzco79xf8");
		try{
			AuthenticationResponse r = httpClient.post(
				"https://login.salesforce.com/services/oauth2/token",
				auth_params,
				AuthenticationResponse.class,
				true);
			accessToken = r.access_token;
			instanceUrl = r.instance_url;
		}catch(HotPadsHttpClientException e){
			logger.error("Could not connect to Salesforce API : " + e.getEntity());
		}
	}
	
	public <D extends SalesforceDatabean<PK>, PK extends SalesforceDatabeanKey> D get(PK key, Class<D> databeanClass){
		String url = instanceUrl + ENDPOINT;
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
	
	public <D extends SalesforceDatabean<PK>, PK extends SalesforceDatabeanKey> void put(D databean){
		String url = instanceUrl + ENDPOINT;
		url += databean.getClass().getSimpleName();
		url += "/";
		url += databean.getKey().getId();
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Bearer " + accessToken);
		httpClient.patch(url, databean, false, headers);
	}
	
	private static class Contact extends SalesforceDatabean<ContactKey>{
		
		public Contact(ContactKey key){
			super(key);
		}

		private String FirstName;
		
	}
	
	private static class ContactKey extends SalesforceDatabeanKey{

		public ContactKey(String id){
			super(id);
		}
		
	}
	
	private static abstract class SalesforceDatabean<PK extends SalesforceDatabeanKey>{
		
		private transient PK key;
		
		public SalesforceDatabean(PK key){
			this.key = key;
		}
		
		public PK getKey(){
			return key;
		}
		
		public void setKey(PK key){
			this.key = key;
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
	
	public static void main(String[] args){
		SalesforceApi salesforceApi = new SalesforceApi();
				
		Contact contact = salesforceApi.get(new ContactKey("003b000000HxnHw"), Contact.class);
		logger.warn(contact.FirstName);
		contact.FirstName = "John";
		salesforceApi.put(contact);
	}
	
}

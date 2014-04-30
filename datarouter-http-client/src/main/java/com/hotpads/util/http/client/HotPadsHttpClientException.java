package com.hotpads.util.http.client;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;


@SuppressWarnings("serial")
public class HotPadsHttpClientException extends RuntimeException{
	
	private int statusCode;
	private String entity;
	
	public HotPadsHttpClientException(HttpResponse response){
		statusCode = response.getStatusLine().getStatusCode();
		try{
			entity = EntityUtils.toString(response.getEntity());
		}catch (ParseException | IOException e){
			throw new RuntimeException(e);
		}
		EntityUtils.consumeQuietly(response.getEntity());
	}

	public HotPadsHttpClientException(Exception e){
		super(e);
	}
	
	public int getStatusCode(){
		return statusCode;
	}
	
	public String getEntity(){
		return entity;
	}

}

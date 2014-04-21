package com.hotpads.util.http.client;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;


@SuppressWarnings("serial")
public class HotPadsHttpClientException extends RuntimeException{

	private static Logger logger = Logger.getLogger(HotPadsHttpClientException.class.getCanonicalName());
	
	private int statusCode;
	
	public HotPadsHttpClientException(HttpResponse response){
		statusCode = response.getStatusLine().getStatusCode();
		logger.warning("Error Code : " + statusCode);
		try{
			logger.warning("Entity : " + EntityUtils.toString(response.getEntity()));
		}catch (ParseException | IOException e){
			e.printStackTrace();
		}
		EntityUtils.consumeQuietly(response.getEntity());
	}

	public HotPadsHttpClientException(Exception e){
		super(e);
		e.printStackTrace();
	}
	
	public int getStatusCode(){
		return statusCode;
	}

}

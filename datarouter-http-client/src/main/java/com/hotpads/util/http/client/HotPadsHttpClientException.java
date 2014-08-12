package com.hotpads.util.http.client;

import java.io.IOException;
import java.util.Scanner;

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
		}catch (ParseException | IOException | IllegalArgumentException e){
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
	
	@Override
	public String toString(){
		String firstLine = "";
		if(getEntity() != null){
			Scanner scanner = new Scanner(getEntity());
			while(scanner.hasNextLine() && firstLine.trim().isEmpty()){
				firstLine = scanner.nextLine();
			}
			scanner.close();
		}
		return super.toString() + "(" + getStatusCode() + ", " + firstLine + ")";
	}

}

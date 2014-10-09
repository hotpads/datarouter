package com.hotpads.util.http.client;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

public class HotPadsHttpResponse {
	private int statusCode;
	private HttpEntity entity;
	
	public HotPadsHttpResponse(HttpResponse response){
		statusCode = response.getStatusLine().getStatusCode();
		entity = response.getEntity();
		EntityUtils.consumeQuietly(entity);
	}

	@Override
	public String toString(){
		String firstLine = "";
		if(getEntity() != null){
			Scanner scanner = new Scanner(getEntityString());
			while(scanner.hasNextLine() && firstLine.trim().isEmpty()){
				firstLine = scanner.nextLine();
			}
			scanner.close();
		}
		return super.toString() + "(" + getStatusCode() + ", " + firstLine + ")";
	}

	public int getStatusCode(){
		return statusCode;
	}
	
	public HttpEntity getEntity(){
		return entity;
	}
	
	public String getEntityString() {
		try {
			return EntityUtils.toString(entity);
		} catch (ParseException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
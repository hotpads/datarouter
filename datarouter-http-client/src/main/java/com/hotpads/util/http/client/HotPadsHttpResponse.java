package com.hotpads.util.http.client;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

public class HotPadsHttpResponse {
	private int statusCode;
	private String entity;
	
	public HotPadsHttpResponse(HttpResponse response) throws ParseException, IllegalArgumentException {
		this.statusCode = response.getStatusLine().getStatusCode();
		this.entity = "";
		
		HttpEntity httpEntity = response.getEntity();
		if(httpEntity != null) {
			// allow exceptions to bubble up
			try{
				this.entity = EntityUtils.toString(httpEntity);
			} catch (final IOException ignore) {
			} finally {
				EntityUtils.consumeQuietly(httpEntity);
			}
		}
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

	public int getStatusCode(){
		return statusCode;
	}
	
	public String getEntity(){
		return entity;
	}
}

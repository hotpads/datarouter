package com.hotpads.util.http.client;


@SuppressWarnings("serial")
public class HotPadsHttpClientException extends RuntimeException{
	
	private HotPadsHttpResponse response;
	
	public HotPadsHttpClientException(HotPadsHttpResponse response){
		super(response.toString());
		this.response = response;
	}

	public HotPadsHttpClientException(Exception e){
		super(e);
	}
	
	public int getStatusCode(){
		if(response==null) return -1;
		return response.getStatusCode();
	}
	
	public String getEntity(){
		if(response==null) return null;
		return response.getEntity();
	}
	
	public HotPadsHttpResponse getResponse(){
		return response;
	}
	
	@Override
	public String toString(){
		if(response==null){
			return super.toString();
		}
		return response.toString();
	}

}

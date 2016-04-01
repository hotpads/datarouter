package com.hotpads.util.http.response;

import java.io.IOException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an abstraction over the HttpResponse that handles several of the expected HTTP failures
 */
public class HotPadsHttpResponse{
	private static final Logger logger = LoggerFactory.getLogger(HotPadsHttpResponse.class);

	private final HttpResponse response;
	private final List<Cookie> cookies;
	private int statusCode;
	private String entity;

	public HotPadsHttpResponse(HttpResponse response, HttpClientContext context){
		this.response = response;
		this.cookies = context.getCookieStore().getCookies();
		if(response != null){
			this.statusCode = response.getStatusLine().getStatusCode();
			this.entity = "";

			HttpEntity httpEntity = response.getEntity();
			if(httpEntity != null){
				try{
					this.entity = EntityUtils.toString(httpEntity);
				}catch(IOException e){
					logger.error("Exception occurred while reading HTTP response entity", e);
				}finally{
					EntityUtils.consumeQuietly(httpEntity);
				}
			}
		}
	}

	public int getStatusCode(){
		return statusCode;
	}

	public String getEntity(){
		return entity;
	}

	public Header getFirstHeader(String name){
		return response.getFirstHeader(name);
	}

	public List<Cookie> getCookies(){
		return cookies;
	}

}

package com.hotpads.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.util.RequestTool;

public class ResponseTool{

	public static void sendError(
			HttpServletResponse response, int code, String message){
		try{
			response.sendError(code, message);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public static void sendRedirect(
			HttpServletRequest request, 
			HttpServletResponse response, 
			int code, 
			String urlPath){
		String fullyQualifiedUrl = urlPath;
		if(!urlPath.contains("://")){
			//really is just path, create fullUrl
			fullyQualifiedUrl = 
				RequestTool.getFullyQualifiedUrl(urlPath, request).toString();
		}
		sendRedirect(response,code,fullyQualifiedUrl);
	}
	
	public static void sendRedirect(
			HttpServletResponse response, int code, String fullyQualifiedUrl){
		response.setStatus(code);
		response.addHeader("Location", fullyQualifiedUrl);
	}
}

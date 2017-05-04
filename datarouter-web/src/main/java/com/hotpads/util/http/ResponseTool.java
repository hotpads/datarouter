package com.hotpads.util.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class ResponseTool{

	public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

	public static void sendError(HttpServletResponse response, int code, String message){
		try{
			response.sendError(code, message);  // html
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static void sendErrorInJsonForMessage(HttpServletResponse response, int code, String message){
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", message);
		jsonObject.addProperty("httpResponseCode", code);
		sendErrorInJson(response, code, jsonObject.toString());
	}

	public static void sendErrorInJson(HttpServletResponse response, int code, String encodedJson){
		response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
		response.setStatus(code);
		try{
			response.getWriter().write(encodedJson);
		}catch(IOException e){
			sendError(response, code, encodedJson);
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
			fullyQualifiedUrl = RequestTool.getFullyQualifiedUrl(urlPath, request).toString();
		}
		sendRedirect(response, code, fullyQualifiedUrl);
	}

	public static void sendRedirect(
			HttpServletResponse response, int code, String fullyQualifiedUrl){
		response.setStatus(code);
		response.addHeader("Location", fullyQualifiedUrl);
	}

	public static PrintWriter getWriter(HttpServletResponse response){
		try{
			return response.getWriter();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}

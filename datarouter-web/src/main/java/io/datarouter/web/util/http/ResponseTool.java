/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.util.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

public class ResponseTool{

	private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

	public static void sendError(HttpServletResponse response, int code, String message){
		try{
			response.sendError(code, message); // html
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static void sendJsonForMessage(HttpServletResponse response, int code, String message) throws IOException{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", message);
		jsonObject.addProperty("httpResponseCode", code);
		sendJson(response, code, jsonObject.toString());
	}

	public static void sendJson(HttpServletResponse response, int code, String body) throws IOException{
		response.setStatus(code);
		sendJson(response, body);
	}

	public static void sendJson(HttpServletResponse response, String body) throws IOException{
		response.setContentType(ResponseTool.CONTENT_TYPE_APPLICATION_JSON);
		new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)
				.append(body)
				.close();
	}

	public static void sendRedirect(
			HttpServletRequest request,
			HttpServletResponse response,
			int code,
			String urlPath){
		String fullyQualifiedUrl = urlPath;
		if(!urlPath.contains("://")){
			// really is just path, create fullUrl
			fullyQualifiedUrl = RequestTool.getFullyQualifiedUrl(urlPath, request).toString();
		}
		sendRedirect(response, code, fullyQualifiedUrl);
	}

	public static void sendRedirect(HttpServletResponse response, int code, String fullyQualifiedUrl){
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

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
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.util.duration.DatarouterDuration;

public class ResponseTool{
	private static final Logger logger = LoggerFactory.getLogger(ResponseTool.class);

	private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
	private static final DatarouterDuration LOG_THRESHOLD = new DatarouterDuration(500, TimeUnit.MILLISECONDS);

	public static void sendError(HttpServletResponse response, int code, String message){
		try{
			response.sendError(code, message); // html
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static void sendJsonForMessage(HttpServletResponse response, int code, String message) throws IOException{
		sendJson(response, code, getJsonForMessage(code, message));
	}

	public static String getJsonForMessage(int code, String message){
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", message);
		jsonObject.addProperty("httpResponseCode", code);
		return jsonObject.toString();
	}

	public static void sendJson(HttpServletResponse response, int code, String body) throws IOException{
		response.setStatus(code);
		sendJson(response, body);
	}

	public static void sendJson(HttpServletResponse response, String body) throws IOException{
		response.setContentType(ResponseTool.CONTENT_TYPE_APPLICATION_JSON);
		// close the writer before the trace to be able to include the close() duration in the measure
		long start = System.currentTimeMillis();
		try(var $ = TracerTool.startSpan("ResponseTool sendJson", TraceSpanGroupType.HTTP);
				OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)){
			writer.append(body);
		}
		DatarouterDuration duration = DatarouterDuration.ageMs(start);
		if(duration.isLongerThan(LOG_THRESHOLD)){
			logger.warn("duration={} durationMs={} size={} traceContext={}",
					duration,
					duration.toMillis(),
					body.length(),
					TracerThreadLocal.opt()
							.flatMap(Tracer::getTraceContext)
							.map(W3TraceContext::getTraceparent)
							.map(Traceparent::toString)
							.orElse(""));
		}
	}

	public static void sendRedirect(HttpServletRequest request, HttpServletResponse response, int code, String urlPath){
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

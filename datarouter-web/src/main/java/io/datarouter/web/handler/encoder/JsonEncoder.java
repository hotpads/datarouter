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
package io.datarouter.web.handler.encoder;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.web.exception.HandledException;
import io.datarouter.web.util.http.ResponseTool;

@Singleton
public class JsonEncoder implements HandlerEncoder{

	private final JsonSerializer jsonSerializer;

	@Inject
	public JsonEncoder(@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER) JsonSerializer jsonSerializer){
		this.jsonSerializer = jsonSerializer;
	}

	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws IOException{
		ResponseTool.sendJson(response, jsonSerializer.serialize(result));
	}

	@Override
	public void sendExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request) throws IOException{
		Object responseBody = exception.getHttpResponseBody();
		if(responseBody == null){
			ResponseTool.sendJsonForMessage(response, exception.getHttpResponseCode(), exception.getMessage());
			return;
		}
		ResponseTool.sendJson(response, exception.getHttpResponseCode(), jsonSerializer.serialize(responseBody));
	}

}

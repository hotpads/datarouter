/*
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
package io.datarouter.graphql.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ExecutionResult;
import io.datarouter.graphql.client.util.response.GraphQlErrorDto;
import io.datarouter.graphql.client.util.response.GraphQlResultDto;
import io.datarouter.graphql.web.exception.GraphQlExceptionRecorder;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.web.handler.encoder.DefaultEncoder;
import io.datarouter.web.handler.encoder.InputStreamHandlerEncoder;
import io.datarouter.web.handler.encoder.JsonEncoder;
import io.datarouter.web.handler.encoder.MavEncoder;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GraphQlEncoder extends DefaultEncoder{
	private static final Logger logger = LoggerFactory.getLogger(GraphQlEncoder.class);

	private final GraphQlExceptionRecorder graphQlExceptionRecorder;

	@Inject
	public GraphQlEncoder(
			MavEncoder mavEncoder,
			InputStreamHandlerEncoder inputStreamHandlerEncoder,
			JsonEncoder jsonEncoder,
			GraphQlExceptionRecorder graphQlExceptionRecorder){
		super(mavEncoder, inputStreamHandlerEncoder, jsonEncoder);
		this.graphQlExceptionRecorder = graphQlExceptionRecorder;
	}

	@Override
	public void finishRequest(
			Object result,
			ServletContext servletContext,
			HttpServletResponse response,
			HttpServletRequest request)
	throws ServletException, IOException{
		GraphQlResultDto<?> wrappedResult = makeApiReponse((ExecutionResult)result, request);
		super.finishRequest(wrappedResult, servletContext, response, request);
	}

	protected GraphQlResultDto<?> makeApiReponse(ExecutionResult result, HttpServletRequest request){
		List<GraphQlErrorDto> errorDto = graphQlExceptionRecorder.recordGraphQlErrors(result.getErrors(), request);
		boolean trace = RequestTool.getBoolean(request, "trace", false);
		if(trace){
			logger.info("traceparent={} extensions={}", TracerTool.getCurrentTraceparent(), result.getExtensions());
			return GraphQlResultDto.withGraphQlExtensions(result.getData(), errorDto, result.getExtensions());
		}
		return GraphQlResultDto.with(result.getData(), errorDto);
	}

}

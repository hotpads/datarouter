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
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.web.exception.HandledException;

public class RawStringEncoder implements HandlerEncoder{

	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request)
	throws IOException{
		PrintWriter writer = response.getWriter();
		writer.print(result);
	}

	@Override
	public void sendExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request)
	throws IOException{
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
	}

}

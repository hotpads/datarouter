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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.web.exception.HandledException;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.util.http.ResponseTool;

public class MavEncoder implements HandlerEncoder{

	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request) throws ServletException, IOException{
		if(result == null){
			return;
		}
		Mav mav = (Mav)result;

		if(mav.isRedirect()){
			response.sendRedirect(mav.getRedirectUrl());
			return;
		}

		response.setContentType(mav.getContentType());
		response.setStatus(mav.getStatusCode());
		// add the model variables as request attributes
		mav.getModel().forEach(request::setAttribute);

		// forward to the jsp
		String targetContextName = mav.getContext();
		String viewName = mav.getViewName();
		ServletContext targetContext = servletContext;
		if(targetContextName != null){
			targetContext = servletContext.getContext(targetContextName);
			throw new RuntimeException("Could not acquire servletContext=" + targetContextName
					+ ".  Make sure context has crossContext=true enabled.");
		}
		RequestDispatcher dispatcher = targetContext.getRequestDispatcher(viewName);
		dispatcher.include(request, response);
	}

	@Override
	public void sendExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request){
		ResponseTool.sendError(response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
	}

}

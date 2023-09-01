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
package io.datarouter.web.user.authenticate.saml;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
// TODO braydonh: figure out a way to move this out of dr-web
public class SamlAssertionConsumerServlet extends HttpServlet{

	@Inject
	private SamlService samlService;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		samlService.consumeAssertion(request, response);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		try{
			response.sendRedirect(getServletContext().getContextPath());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}

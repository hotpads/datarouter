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
package io.datarouter.web.html.j2html.bootstrap4;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.web.handler.mav.Mav;

@Singleton
public class Bootstrap4HomepageCreator{

	@Inject
	private DatarouterService datarouterService;
	@Inject
	private Bootstrap4PageFactory factory;

	public Mav buildHomepageMav(HttpServletRequest request){
		var h1 = h1(datarouterService.getName()).withClass("text-capitalize");
		var container = div(h1).withClass("container my-5");
		return factory.startBuilder(request)
				.withTitle(datarouterService.getName())
				.withContent(container)
				.buildMav();
	}

}

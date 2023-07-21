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
package io.datarouter.auth.readme;

import static j2html.TagCreator.div;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class AuthHellowWorldHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler
	public Mav helloWorld(){
		var content = div("Hello World");
		return pageFactory.startBuilder(request)
				.withTitle("Hello World")
				.withContent(content)
				.buildMav();
	}

}

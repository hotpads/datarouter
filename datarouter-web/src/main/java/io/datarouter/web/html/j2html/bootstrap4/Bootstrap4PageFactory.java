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
import static j2html.TagCreator.h5;
import static j2html.TagCreator.pre;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.MavPropertiesFactory;
import j2html.tags.DomContent;

@Singleton
public class Bootstrap4PageFactory{

	@Inject
	private MavPropertiesFactory mavPropertiesFactory;

	public Bootstrap4PageBuilder startBuilder(HttpServletRequest request){
		return new Bootstrap4PageBuilder()
				.withMavProperties(mavPropertiesFactory.buildAndSet(request));
	}

	public Mav message(HttpServletRequest request, String message){
		var content = div(h5(message))
				.withStyle("margin:40px;");
		return startBuilder(request)
				.withTitle("Message")
				.withContent(content)
				.buildMav();
	}

	public Mav message(HttpServletRequest request, DomContent message){
		var content = div(message);
		return startBuilder(request)
				.withTitle("Message")
				.withContent(content)
				.buildMav();
	}

	public Mav preformattedMessage(HttpServletRequest request, String message){
		var content = div(pre(message))
				.withStyle("margin:40px;");
		return startBuilder(request)
				.withTitle("Message")
				.withContent(content)
				.buildMav();
	}

}

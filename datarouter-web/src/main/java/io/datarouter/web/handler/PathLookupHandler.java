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
package io.datarouter.web.handler;

import java.util.Optional;

import io.datarouter.web.dispatcher.DefaultDispatcherServlet;
import io.datarouter.web.dispatcher.Dispatcher;
import io.datarouter.web.dispatcher.Dispatcher.HandlerDto;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.FormTag;
import jakarta.inject.Inject;

public class PathLookupHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	public Dispatcher dispatcher;
	@Inject
	public DefaultDispatcherServlet dispatcherServlet;

	@Handler
	public Mav handlerSearch(Optional<String> path){
		return pageFactory.simplePage(request, "Handler Search", makeContent(path.orElse("")));
	}

	private DivTag makeContent(String path){
		return TagCreator.div(makeForm(path), makeResult(path))
				.withClass("mt-4")
				.withStyle("width: 700px; margin: 0 auto;");
	}

	private FormTag makeForm(String path){
		var prependText = TagCreator.div(servletContext.getContextPath())
				.withClass("input-group-text");
		var inputGroupPrepend = TagCreator.div(prependText)
				.withClass("input-group-prepend");
		var input = TagCreator.input()
				.withClass("form-control")
				.withPlaceholder("url path")
				.withType("text")
				.withName("path")
				.withValue(path);
		var inputGroup = TagCreator.div(inputGroupPrepend, input)
				.withClass("input-group");
		var formGroup = TagCreator.div(inputGroup)
				.withClass("form-group flex-grow-1 mr-2 mb-0");
		var submitButton = TagCreator.button("Search")
				.withClass("btn btn-primary")
				.withType("submit");
		var formRow = TagCreator.div(formGroup, submitButton)
				.withClass("form-row align-items-center");

		return TagCreator.form(formRow);
	}

	private DivTag makeResult(String path){
		var fullPath = servletContext.getContextPath() + path;
		String result = estimateHandlerByPath(fullPath);

		var resultUrl = TagCreator.p(TagCreator.span("Result for: "), TagCreator.em(fullPath));
		var resultHandler = TagCreator.p(TagCreator.span("Handler: "), TagCreator.em(result));

		return TagCreator.div(resultUrl, resultHandler)
				.withClass("mt-4");
	}

	private String estimateHandlerByPath(String path){
		for(RouteSet routeSet : dispatcherServlet.getRouteSets()){
			Optional<HandlerDto> dto = dispatcher.estimateHandlerForPath(path, routeSet);
			if(dto.isPresent()){
				return dto.get().className() + "." + dto.get().methodName();
			}
		}

		return "No Handler Found";
	}
}

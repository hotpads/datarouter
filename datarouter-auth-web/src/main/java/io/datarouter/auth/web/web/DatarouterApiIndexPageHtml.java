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
package io.datarouter.auth.web.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.li;
import static j2html.TagCreator.span;
import static j2html.TagCreator.ul;

import java.util.List;
import java.util.Map;

import io.datarouter.web.handler.documentation.DocumentedEndpointJspDto;
import j2html.tags.DomContent;

public class DatarouterApiIndexPageHtml{

	public static DomContent makeApiIndexContent(Map<String,List<DocumentedEndpointJspDto>> endpointsByDispatchType){
		return div()
				.with(div()
						.withClass("d-lg-flex d-block")
						.with(div()
								.withStyle("width: 200px; flex-shrink: 0")
								.withClass("px-2 border-right py-4")
								.with(buildTableOfContents(endpointsByDispatchType)))
						.with(div()
								.with(h2("API Documentation"))
								.withClass("mx-2 py-4")
								.withStyle("flex-grow: 1")
								.with(div(each(endpointsByDispatchType.entrySet(),
										DatarouterApiIndexPageHtml::createDispatchTypeSection)))));
	}

	private static DomContent buildTableOfContents(Map<String,List<DocumentedEndpointJspDto>> endpointsByDispatchType){
		return div()
				.withStyle("position: sticky; top: 0")
				.with(div(each(endpointsByDispatchType.keySet(),
						dispatchType -> div()
								.withClass("mb-1")
								.withStyle("white-space: nowrap; overflow: hidden; text-overflow: ellipsis")
								.with(a(dispatchType + "s")
										.withHref("#" + dispatchType)))));
	}

	private static DomContent createDispatchTypeSection(Map.Entry<String,List<DocumentedEndpointJspDto>> entry){
		return div()
				.with(h4(entry.getKey() + "s")
						.withId(entry.getKey()))
				.withClass("my-4")
				.with(createEndpointList(entry.getValue()));
	}

	private static DomContent createEndpointList(List<DocumentedEndpointJspDto> endpoints){
		return ul(each(endpoints, DatarouterApiIndexPageHtml::createEndpointItem))
				.withClass("list-group list-group-flush");
	}

	private static DomContent createEndpointItem(DocumentedEndpointJspDto endpoint){
		return li()
				.withClass("list-group-item")
				.with(
						div()
								.withClass("d-lg-flex d-block justify-content-between align-items-center")
								.with(a(endpoint.getUrl())
										.withStyle(endpoint.getIsDeprecated() || !endpoint.getDeprecatedOn().isEmpty()
												? "text-decoration: line-through" : "")
										.withHref("?endpoint=" + endpoint.getUrl()))
								.with(span()
										.withClass("text-muted d-block mt-1 ml-3")
										.withText(endpoint.getDescription())));
	}
}

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
package io.datarouter.web.navigation;

import java.util.Objects;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.RequestDurationTool;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

//for bootstrap 4
public class DatarouterNavbarV2Html{

	private final MavProperties props;

	public DatarouterNavbarV2Html(MavProperties props){
		this.props = Objects.requireNonNull(props);
	}

	public ContainerTag build(){
		boolean collapsible = props.getTomcatWebApps().size() > 1;
		String collapsibleStyle = collapsible ? "navbar-expand-md py-md-0" : "navbar-expand py-0";
		String productionStyle = props.getIsProduction() ? "productionEnv" : "";
		var nav = TagCreator.nav()
				.withId("common-navbar")
				.withClasses("navbar", collapsibleStyle, "navbar-dark", "bg-dark", productionStyle);
		var button = TagCreator.button()
				.withClass("navbar-toggler ml-auto")
				.withType("button")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#common-navbar-content");
		var span = TagCreator.span()
				.withClass("navbar-toggler-icon");
		var div = TagCreator.div()
				.withId("common-navbar-content")
				.withClass("collapse navbar-collapse");
		var ul = TagCreator.ul()
				.withClass("navbar-nav");
		ul.with(makeWebappList()).with(makeAfterWebappList());
		return nav
				.with(button.with(span))
				.with(div.with(ul));
	}

	private ContainerTag[] makeWebappList(){
		return props.getTomcatWebApps().entrySet().stream()
				.map(webapp -> makeWebappListItem(webapp.getKey(), webapp.getValue()))
				.toArray(ContainerTag[]::new);
	}

	private ContainerTag makeWebappListItem(String name, String href){
		var li = TagCreator.li()
				.withClass("nav-item");
		var link = TagCreator.a(name)
				.withClass("nav-link")
				.attr("data-target", name)
				.withHref(href);
		return li.with(link);
	}

	private ContainerTag[] makeAfterWebappList(){
		var divider = TagCreator.li()
				.withClass("border-right border-secondary d-none d-md-block");
		var li = TagCreator.li()
				.withClass("nav-item");
		var datarouterLink = TagCreator.a("datarouter")
				.withClass("nav-link")
				.attr("data-target", "datarouter")
				.withHref(props.getContextPath() + "/datarouter");
		return new ContainerTag[]{
				divider,
				li.with(datarouterLink),
				divider,
				makeTraceSection()};
	}

	private ContainerTag makeTraceSection(){
		String traceHref = RequestAttributeTool.get(props.getRequest(), BaseHandler.TRACE_URL_REQUEST_ATTRIBUTE).orElse(
				"");
		String javaTime = RequestDurationTool.getRequestElapsedDurationString(props.getRequest()).orElse("?");
		var li = TagCreator.li()
				.withStyle("nav-item");
		var link = TagCreator.a()
				.withClass("nav-link")
				.withHref(traceHref);
		var java = TagCreator.span("j: " + javaTime)
				.withTitle("Java processing duration");
		var requestTimingJsHook = TagCreator.span()
				.withId("requestTiming");
		var requestDuration = TagCreator.span()
				.withTitle("Request duration")
				.withText("r:");
		var clientTimingJsHook = TagCreator.span()
				.withId("clientTiming");
		var clientDuration = TagCreator.span()
				.withTitle("Client document load duration")
				.withText("c:");
		return li.with(link.with(
				java,
				requestDuration.with(requestTimingJsHook),
				clientDuration.with(clientTimingJsHook)));
	}

}

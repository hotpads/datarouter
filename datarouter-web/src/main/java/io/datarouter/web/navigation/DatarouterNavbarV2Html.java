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

import static j2html.TagCreator.a;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.li;
import static j2html.TagCreator.nav;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;
import static j2html.TagCreator.ul;

import java.util.Objects;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.RequestDurationTool;
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
		var span = span()
				.withClass("navbar-toggler-icon");
		var button = button(span)
				.withClass("navbar-toggler ml-auto")
				.withType("button")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#common-navbar-content");
		var ul = ul()
				.withClass("navbar-nav")
				.with(makeWebappList())
				.with(makeAfterWebappList());
		var div = div(ul)
				.withId("common-navbar-content")
				.withClass("collapse navbar-collapse");
		return nav(button, div)
				.withId("common-navbar")
				.withClasses("navbar", collapsibleStyle, "navbar-dark", "bg-dark", productionStyle);
	}

	private ContainerTag[] makeWebappList(){
		return props.getTomcatWebApps().entrySet().stream()
				.map(webapp -> makeWebappListItem(webapp.getKey(), webapp.getValue()))
				.toArray(ContainerTag[]::new);
	}

	private ContainerTag makeWebappListItem(String name, String href){
		var link = a(name)
				.withClass("nav-link")
				.attr("data-target", name)
				.withHref(href);
		return li(link)
				.withClass("nav-item");
	}

	private ContainerTag[] makeAfterWebappList(){
		var divider = li()
				.withClass("border-right border-secondary d-none d-md-block");
		var datarouterLink = a("datarouter")
				.withClass("nav-link")
				.attr("data-target", "datarouter")
				.withHref(props.getContextPath() + "/datarouter");
		var li = li(datarouterLink)
				.withClass("nav-item");
		return new ContainerTag[]{divider, li, divider, makeTraceSection()};
	}

	private ContainerTag makeTraceSection(){
		String traceHref = RequestAttributeTool.get(props.getRequest(), BaseHandler.TRACE_URL_REQUEST_ATTRIBUTE).orElse(
				"");
		String javaTime = RequestDurationTool.getRequestElapsedDurationString(props.getRequest()).orElse("?");
		var javaDuration = span("j: " + javaTime)
				.withTitle("Java processing duration");
		var requestTimingJsHook = span()
				.withId("requestTiming");
		var requestDuration = span(text("r:"), requestTimingJsHook)
				.withTitle("Request duration");
		var clientTimingJsHook = span()
				.withId("clientTiming");
		var clientDuration = span(text("c:"), clientTimingJsHook)
				.withTitle("Client document load duration");
		var link = a(javaDuration, requestDuration, clientDuration)
				.withClass("nav-link")
				.withHref(traceHref);
		return li(link)
				.withStyle("nav-item");
	}

}

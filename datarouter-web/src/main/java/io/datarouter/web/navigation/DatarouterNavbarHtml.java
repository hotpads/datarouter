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
import j2html.tags.specialized.LiTag;
import j2html.tags.specialized.NavTag;

//for bootstrap 3
public class DatarouterNavbarHtml{

	private final MavProperties props;

	public DatarouterNavbarHtml(MavProperties props){
		this.props = Objects.requireNonNull(props);
	}

	public NavTag build(){
		String productionStyle = props.getIsProduction() ? "productionEnv" : "";
		var span = span()
				.withClass("icon-bar");
		var button = button(span, span, span)
				.withClass("navbar-toggle collapsed")
				.withType("button")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#common-navbar");
		var ul = ul()
				.with(makeWebappList())
				.with(makeAfterWebappList())
				.withClass("nav navbar-nav");
		var div = div(ul)
				.withId("common-navbar")
				.withClass("collapse navbar-collapse");
		var container = div(button, div)
				.withClass("container-fluid");
		return nav(container)
				.withClasses("navbar", "navbar-inverse", "navbar-static-top", "navbar-thin", productionStyle);
	}

	private ContainerTag<?>[] makeWebappList(){
		return props.getTomcatWebApps().entrySet().stream()
				.map(webapp -> makeWebappListItem(webapp.getKey(), webapp.getValue()))
				.toArray(ContainerTag[]::new);
	}

	private LiTag makeWebappListItem(String name, String href){
		var link = a(name)
				.withHref(href);
		return li(link)
				.withId("common-menu-" + name);
	}

	private ContainerTag<?>[] makeAfterWebappList(){
		var divider = li()
				.withClass("divider-vertical");
		var datarouterLink = a("datarouter")
				.withHref(props.getContextPath() + "/datarouter");
		var li = li(datarouterLink)
				.withClass("common-menu-datarouter");
		return new ContainerTag[]{divider, li, divider, makeTraceSection()};
	}

	private LiTag makeTraceSection(){
		String traceHref = RequestAttributeTool.get(props.getRequest(), BaseHandler.TRACE_URL_REQUEST_ATTRIBUTE).orElse(
				"");
		String javaTime = RequestDurationTool.getRequestElapsedDurationString(props.getRequest()).orElse("?");
		var javaDuration = span("j: " + javaTime);
		var requestTimingJsHook = span()
				.withId("requestTiming");
		var requestDuration = span(text("r:"), requestTimingJsHook);
		var clientTimingJsHook = span()
				.withId("clientTiming");
		var clientDuration = span(text("c:"), clientTimingJsHook);
		var link = a(javaDuration, requestDuration, clientDuration)
				.withHref(traceHref);
		return li(link);
	}

}

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

//for bootstrap 3
public class DatarouterNavbarHtml{

	private final MavProperties props;

	public DatarouterNavbarHtml(MavProperties props){
		this.props = Objects.requireNonNull(props);
	}

	public ContainerTag build(){
		String productionStyle = props.getIsProduction() ? "productionEnv" : "";
		var nav = TagCreator.nav()
				.withClasses("navbar", "navbar-inverse", "navbar-static-top", "navbar-thin", productionStyle);
		var container = TagCreator.div()
				.withClass("container-fluid");
		var button = TagCreator.button()
				.withClass("navbar-toggle collapsed")
				.withType("button")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#common-navbar");
		var span = TagCreator.span()
				.withClass("icon-bar");
		var div = TagCreator.div()
				.withId("common-navbar")
				.withClass("collapse navbar-collapse");
		var ul = TagCreator.ul()
				.withClass("nav navbar-nav");
		ul.with(makeWebappList()).with(makeAfterWebappList());
		return nav.with(container
				.with(button.with(span).with(span).with(span))
				.with(div.with(ul)));
	}

	private ContainerTag[] makeWebappList(){
		return props.getTomcatWebApps().entrySet().stream()
				.map(webapp -> makeWebappListItem(webapp.getKey(), webapp.getValue()))
				.toArray(ContainerTag[]::new);
	}

	private ContainerTag makeWebappListItem(String name, String href){
		var li = TagCreator.li()
				.withId("common-menu-" + name);
		var link = TagCreator.a(name)
				.withHref(href);
		return li.with(link);
	}

	private ContainerTag[] makeAfterWebappList(){
		var divider = TagCreator.li()
				.withClass("divider-vertical");
		var li = TagCreator.li()
				.withClass("common-menu-datarouter");
		var datarouterLink = TagCreator.a("datarouter")
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
		var li = TagCreator.li();
		var link = TagCreator.a()
				.withHref(traceHref);
		var java = TagCreator.span("j: " + javaTime);
		var requestTimingJsHook = TagCreator.span()
				.withId("requestTiming");
		var requestDuration = TagCreator.span()
				.withText("r:");
		var clientTimingJsHook = TagCreator.span()
				.withId("clientTiming");
		var clientDuration = TagCreator.span()
				.withText("c:");
		return li.with(link.with(
				java,
				requestDuration.with(requestTimingJsHook),
				clientDuration.with(clientTimingJsHook)));
	}

}

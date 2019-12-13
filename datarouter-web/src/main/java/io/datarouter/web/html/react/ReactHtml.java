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
package io.datarouter.web.html.react;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.html;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.script;
import static j2html.TagCreator.title;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;

public class ReactHtml{

	private static final List<String> EXTERNAL_REACT_SCRIPTS = Arrays.asList(
			"https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react.js",
			"https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react-dom.js",
			"https://cdnjs.cloudflare.com/ajax/libs/react-router/3.0.2/ReactRouter.min.js",
			"https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/6.24.0/babel.min.js");

	//head
	private final EmptyTag[] datarouterWebCssImports;
	private final ContainerTag datarouterWebRequireJsImport;
	private final ContainerTag datarouterWebRequireJsConfig;
	private final ContainerTag requireScript;
	private final EmptyTag[] datarouterNavbarCssImports;
	private final ContainerTag datarouterNavbarRequestTimingJsImport;
	private final ContainerTag datarouterNavbarRequestTimingScript;
	private final String title;
	//react
	private final String reactScriptPath;
	private final Map<String,String> jsConstants;
	//body header
	private final ContainerTag datarouterNavbar;
	private final ContainerTag webappNavbar;

	public ReactHtml(
			EmptyTag[] datarouterWebCssImports,
			ContainerTag datarouterWebRequireJsImport,
			ContainerTag datarouterWebRequireJsConfig,
			ContainerTag requireScript,
			EmptyTag[] datarouterNavbarCssImports,
			ContainerTag datarouterNavbarRequestTimingJsImport,
			ContainerTag datarouterNavbarRequestTimingScript,
			String title,
			String reactScriptPath,
			Map<String,String> jsConstants,
			ContainerTag datarouterNavbar,
			ContainerTag webappNavbar){
		this.datarouterWebCssImports = datarouterWebCssImports;
		this.datarouterWebRequireJsImport = datarouterWebRequireJsImport;
		this.datarouterWebRequireJsConfig = datarouterWebRequireJsConfig;
		this.requireScript = requireScript;
		this.datarouterNavbarCssImports = datarouterNavbarCssImports;
		this.datarouterNavbarRequestTimingJsImport = datarouterNavbarRequestTimingJsImport;
		this.datarouterNavbarRequestTimingScript = datarouterNavbarRequestTimingScript;
		this.title = title;
		this.reactScriptPath = reactScriptPath;
		this.jsConstants = jsConstants;
		this.datarouterNavbar = datarouterNavbar;
		this.webappNavbar = webappNavbar;
	}

	public ContainerTag build(){
		return html(makeHead(), makeBody());
	}

	private ContainerTag makeHead(){
		var meta = meta()
				.withName("viewport")
				.withContent("width=device-width, initial-scale=1");
		var script = script()
				.withType("text/babel")
				.withSrc(reactScriptPath);
		return head()
				.with(meta)
				.with(makeExternalReactScriptTags())
				.with(datarouterWebCssImports)
				.with(datarouterWebRequireJsImport)
				.with(datarouterWebRequireJsConfig)
				.with(requireScript)
				.with(datarouterNavbarCssImports)
				.with(datarouterNavbarRequestTimingJsImport)
				.with(datarouterNavbarRequestTimingScript)
				.with(title(title))
				.with(makeJsConstantScript())
				.with(script);
	}

	private static ContainerTag[] makeExternalReactScriptTags(){
		return EXTERNAL_REACT_SCRIPTS.stream()
				.map(src -> script().withCharset("UTF-8").withSrc(src))
				.toArray(ContainerTag[]::new);
	}

	private ContainerTag makeJsConstantScript(){
		var script = script();
		jsConstants.entrySet().stream()
				.map(entry -> String.format("const %S = \"%s\";", entry.getKey(), entry.getValue()))
				.map(TagCreator::rawHtml)
				.forEach(script::with);
		return script;
	}

	private ContainerTag makeBody(){
		var app = div()
				.withId("app");
		return body(header(datarouterNavbar, webappNavbar), app);
	}

}

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
		var html = TagCreator.html();
		return html.with(makeHead(), makeBody());
	}

	private ContainerTag makeHead(){
		var head = TagCreator.head();
		var meta = TagCreator.meta()
				.withName("viewport")
				.withContent("width=device-width, initial-scale=1");
		head.with(meta);
		head.with(makeExternalReactScriptTags());
		head.with(datarouterWebCssImports);
		head.with(datarouterWebRequireJsImport);
		head.with(datarouterWebRequireJsConfig);
		head.with(requireScript);
		head.with(datarouterNavbarCssImports);
		head.with(datarouterNavbarRequestTimingJsImport);
		head.with(datarouterNavbarRequestTimingScript);
		head.with(TagCreator.title(title));
		head.with(makeJsConstantScript());
		head.with(TagCreator.script()
				.withType("text/babel")
				.withSrc(reactScriptPath));
		return head;
	}

	private static ContainerTag[] makeExternalReactScriptTags(){
		return EXTERNAL_REACT_SCRIPTS.stream()
				.map(src -> TagCreator.script().withCharset("UTF-8").withSrc(src))
				.toArray(ContainerTag[]::new);
	}

	private ContainerTag makeJsConstantScript(){
		var script = TagCreator.script();
		jsConstants.entrySet().stream()
				.map(entry -> String.format("const %S = \"%s\";", entry.getKey(), entry.getValue()))
				.map(TagCreator::rawHtml)
				.forEach(script::with);
		return script;
	}

	private ContainerTag makeBody(){
		var body = TagCreator.body();
		var header = TagCreator.header();
		var app = TagCreator.div()
				.withId("app");
		return body.with(
				header.with(datarouterNavbar).with(webappNavbar),
				app);
	}

}

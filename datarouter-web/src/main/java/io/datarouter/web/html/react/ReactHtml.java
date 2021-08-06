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
package io.datarouter.web.html.react;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.html;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.script;
import static j2html.TagCreator.title;

import java.util.List;
import java.util.Map;

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;

public class ReactHtml{

	//head
	private final List<String> externalReactScripts;
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
	private final Map<String,String> jsStringConstants;
	private final Map<String,String> jsRawConstants;
	//body header
	private final ContainerTag datarouterNavbar;
	private final ContainerTag webappNavbar;

	public ReactHtml(
			List<String> externalReactScripts,
			EmptyTag[] datarouterWebCssImports,
			ContainerTag datarouterWebRequireJsImport,
			ContainerTag datarouterWebRequireJsConfig,
			ContainerTag requireScript,
			EmptyTag[] datarouterNavbarCssImports,
			ContainerTag datarouterNavbarRequestTimingJsImport,
			ContainerTag datarouterNavbarRequestTimingScript,
			String title,
			String reactScriptPath,
			Map<String,String> jsStringConstants,
			Map<String,String> jsRawConstants,
			ContainerTag datarouterNavbar,
			ContainerTag webappNavbar){
		this.externalReactScripts = externalReactScripts;
		this.datarouterWebCssImports = datarouterWebCssImports;
		this.datarouterWebRequireJsImport = datarouterWebRequireJsImport;
		this.datarouterWebRequireJsConfig = datarouterWebRequireJsConfig;
		this.requireScript = requireScript;
		this.datarouterNavbarCssImports = datarouterNavbarCssImports;
		this.datarouterNavbarRequestTimingJsImport = datarouterNavbarRequestTimingJsImport;
		this.datarouterNavbarRequestTimingScript = datarouterNavbarRequestTimingScript;
		this.title = title;
		this.reactScriptPath = reactScriptPath;
		this.jsStringConstants = jsStringConstants;
		this.jsRawConstants = jsRawConstants;
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

	private ContainerTag[] makeExternalReactScriptTags(){
		return externalReactScripts.stream()
				.map(src -> script().withCharset("UTF-8").withSrc(src))
				.toArray(ContainerTag[]::new);
	}

	private ContainerTag makeJsConstantScript(){
		var script = script();
		addJsConstantsToScript(jsStringConstants, "const %S = \"%s\";", script);
		addJsConstantsToScript(jsRawConstants, "const %S = %s;", script);
		return script;
	}

	private static void addJsConstantsToScript(Map<String,String> constants, String format, ContainerTag script){
		constants.entrySet().stream()
				.map(entry -> String.format(format, entry.getKey(), entry.getValue()))
				.map(TagCreator::rawHtml)
				.forEach(script::with);
	}

	private ContainerTag makeBody(){
		var app = div()
				.withId("app");
		return body(header(datarouterNavbar, webappNavbar), app);
	}

}

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
package io.datarouter.web.html.j2html;

import static j2html.TagCreator.head;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.title;

import java.util.Map;
import java.util.Map.Entry;

import j2html.attributes.Attr;
import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;

public class DatarouterPageHead{

	private final EmptyTag[] datarouterWebCssImports;
	private final ContainerTag datarouterWebRequireJsImport;
	private final ContainerTag datarouterWebRequireJsConfig;
	private final ContainerTag webappRequireJsConfig;
	private final ContainerTag requireScript;
	private final EmptyTag[] datarouterNavbarCssImports;
	private final ContainerTag datarouterNavbarRequestTimingJsImport;
	private final ContainerTag datarouterNavbarRequestTimingScript;
	private final String title;
	private final Map<String,String> httpEquivs;

	public DatarouterPageHead(
			EmptyTag[] datarouterWebCssImports,
			ContainerTag datarouterWebRequireJsImport,
			ContainerTag datarouterWebRequireJsConfig,
			ContainerTag webappRequireJsConfig,
			ContainerTag requireScript,
			EmptyTag[] datarouterNavbarCssImports,
			ContainerTag datarouterNavbarRequestTimingJsImport,
			ContainerTag datarouterNavbarRequestTimingScript,
			String title,
			Map<String,String> httpEquivs){
		this.datarouterWebCssImports = datarouterWebCssImports;
		this.datarouterWebRequireJsImport = datarouterWebRequireJsImport;
		this.datarouterWebRequireJsConfig = datarouterWebRequireJsConfig;
		this.webappRequireJsConfig = webappRequireJsConfig;
		this.requireScript = requireScript;
		this.datarouterNavbarCssImports = datarouterNavbarCssImports;
		this.datarouterNavbarRequestTimingJsImport = datarouterNavbarRequestTimingJsImport;
		this.datarouterNavbarRequestTimingScript = datarouterNavbarRequestTimingScript;
		this.title = title;
		this.httpEquivs = httpEquivs;
	}

	public ContainerTag build(){
		var meta = meta()
				.withName("viewport")
				.withContent("width=device-width, initial-scale=1");
		var head = head()
				.with(meta);
		for(Entry<String,String> httpEquivEntry : httpEquivs.entrySet()){
			var httpEquiv = meta()
					.attr(Attr.HTTP_EQUIV, httpEquivEntry.getKey())
					.withContent(httpEquivEntry.getValue());
			head.with(httpEquiv);
		}
		return head
				.with(datarouterWebCssImports)
				.with(datarouterWebRequireJsImport)
				.with(datarouterWebRequireJsConfig)
				.with(webappRequireJsConfig)
				.with(requireScript)
				.with(datarouterNavbarCssImports)
				.with(datarouterNavbarRequestTimingJsImport)
				.with(datarouterNavbarRequestTimingScript)
				.with(title(title));
	}

}

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
package io.datarouter.web.html.email;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.img;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.util.Arrays;
import java.util.List;

import j2html.tags.ContainerTag;

public class J2HtmlDatarouterEmail{

	private final boolean includeLogo;
	private final String logoImgSrc;
	private final String logoHref;
	private final String title;
	private final String titleHref;
	private final ContainerTag content;

	public J2HtmlDatarouterEmail(
			boolean includeLogo,
			String logoImgSrc,
			String logoHref,
			String title,
			String titleHref,
			ContainerTag content){
		this.includeLogo = includeLogo;
		this.logoImgSrc = logoImgSrc;
		this.logoHref = logoHref;
		this.title = title;
		this.titleHref = titleHref;
		this.content = content;
	}

	public ContainerTag build(){
		return body(makeHeader(), content, makeFooter())
				.withStyle(String.join("", makeBodyStyles()));
	}

	private ContainerTag makeHeader(){
		var titleLink = a(title)
				.withHref(titleHref)
				.withStyle(String.join("", makeTitleStyles()));
		if(!includeLogo){
			return titleLink;
		}
		var logoImg = img()
				.withSrc(logoImgSrc)
				.withStyle(String.join("", makeLogoImgStyles()));
		var logoLink = a(logoImg)
				.withHref(logoHref);
		return table(tr(td(logoLink), td(titleLink)));
	}

	private ContainerTag makeFooter(){
		return div("eZEjPLFSzS")//unique string for email filters
				.withStyle(String.join("", makeFilterStringStyles()));
	}

	/*---------- non-static-final styles for hot code swap -----------*/

	private static final List<String> makeBodyStyles(){
		return Arrays.asList(
				"font-family:Arial;");
	}

	private static List<String> makeLogoImgStyles(){
		return Arrays.asList(
				"display:inline;",
				"height:40px;");
	}

	private static final List<String> makeTitleStyles(){
		return Arrays.asList(
				"text-decoration:none;",
				"color:black;",
				"font-size:24px;",
				"font-weight:bold;",
				"padding:20px 0 0 10px;");
	}

	private static List<String> makeFilterStringStyles(){
		return Arrays.asList(
				"display:none;");
	}

}

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
package io.datarouter.email.html;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.img;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.util.List;

import j2html.tags.DomContent;
import j2html.tags.specialized.BodyTag;
import j2html.tags.specialized.DivTag;

public class J2HtmlDatarouterEmail{

	private final boolean includeLogo;
	private final String logoImgSrc;
	private final String logoHref;
	private final String title;
	private final String titleHref;
	private final DomContent content;

	public final String fromEmail;
	public final boolean fromAdmin;

	public final List<String> toEmails;
	public final boolean toAdmin;
	public final boolean toSubscribers;

	public J2HtmlDatarouterEmail(
			boolean includeLogo,
			String logoImgSrc,
			String logoHref,
			String title,
			String titleHref,
			DomContent content,

			String fromEmail,
			boolean fromAdmin,

			List<String> toEmails,
			boolean toAdmin,
			boolean toSubscribers){
		this.includeLogo = includeLogo;
		this.logoImgSrc = logoImgSrc;
		this.logoHref = logoHref;
		this.title = title;
		this.titleHref = titleHref;
		this.content = content;

		this.fromEmail = fromEmail;
		this.fromAdmin = fromAdmin;

		this.toEmails = toEmails;
		this.toAdmin = toAdmin;
		this.toSubscribers = toSubscribers;
	}

	public BodyTag build(){
		return body(makeHeader(), content, makeFooter())
				.withStyle(String.join("", makeBodyStyles()));
	}

	public BodyTag buildWithoutFooter(){
		return body(makeHeader(), content)
				.withStyle(String.join("", makeBodyStyles()));
	}

	private DomContent makeHeader(){
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

	private DivTag makeFooter(){
		return div("eZEjPLFSzS")//unique string for email filters
				.withStyle(String.join("", makeFilterStringStyles()));
	}

	/*---------- non-static-final styles for hot code swap -----------*/

	private static List<String> makeBodyStyles(){
		return List.of(
				"font-family:Arial;");
	}

	private static List<String> makeLogoImgStyles(){
		return List.of(
				"display:inline;",
				"height:40px;");
	}

	private static List<String> makeTitleStyles(){
		return List.of(
				"text-decoration:none;",
				"color:black;",
				"font-size:24px;",
				"font-weight:bold;",
				"padding:20px 0 0 10px;");
	}

	private static List<String> makeFilterStringStyles(){
		return List.of(
				"display:none;");
	}

}

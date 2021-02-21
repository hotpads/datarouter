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

import j2html.tags.ContainerTag;

public class J2HtmlDatarouterEmailBuilder{

	private String webappName;
	private String environment;
	private String subject;
	private boolean includeLogo = true;
	private String logoImgSrc;
	private String logoHref;
	private String title;
	private String titleHref;
	private ContainerTag content;

	public J2HtmlDatarouterEmailBuilder withWebappName(String webappName){
		this.webappName = webappName;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withEnvironment(String environment){
		this.environment = environment;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withSubject(String subject){
		this.subject = subject;
		return this;
	}

	public String getSubject(){
		return subject == null ? String.format("%s - %s - %s", title, environment, webappName) : subject;
	}

	public J2HtmlDatarouterEmailBuilder withIncludeLogo(boolean includeLogo){
		this.includeLogo = includeLogo;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withLogoImgSrc(String logoImgSrc){
		this.logoImgSrc = logoImgSrc;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withLogoHref(String logoHref){
		this.logoHref = logoHref;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withTitle(String title){
		this.title = title;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withTitleHref(String titleHref){
		this.titleHref = titleHref;
		return this;
	}

	public J2HtmlDatarouterEmailBuilder withContent(ContainerTag content){
		this.content = content;
		return this;
	}

	public J2HtmlDatarouterEmail build(){
		return new J2HtmlDatarouterEmail(includeLogo, logoImgSrc, logoHref, title, titleHref, content);
	}

}
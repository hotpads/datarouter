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

import static j2html.TagCreator.body;
import static j2html.TagCreator.header;

import j2html.tags.ContainerTag;

public class DatarouterPageBody{

	private final ContainerTag datarouterNavbar;
	private final ContainerTag webappNavbar;
	private final ContainerTag content;

	public DatarouterPageBody(
			ContainerTag datarouterNavbar,
			ContainerTag webappNavbar,
			ContainerTag content){
		this.datarouterNavbar = datarouterNavbar;
		this.webappNavbar = webappNavbar;
		this.content = content;
	}

	public ContainerTag build(){
		var header = header(datarouterNavbar, webappNavbar);
		return body(header, content);
	}

}

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
package io.datarouter.web.html.j2html;

import static j2html.TagCreator.body;
import static j2html.TagCreator.header;

import j2html.tags.DomContent;
import j2html.tags.specialized.BodyTag;

public class DatarouterPageBody{

	private final DomContent[] navbars;
	private final DomContent content;

	public DatarouterPageBody(DomContent[] navbars, DomContent content){
		this.navbars = navbars;
		this.content = content;
	}

	public BodyTag build(){
		var header = header(navbars);
		return body(header, content);
	}

}

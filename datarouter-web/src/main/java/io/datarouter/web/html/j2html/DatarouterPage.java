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

import static j2html.TagCreator.html;

import j2html.tags.ContainerTag;

public class DatarouterPage{

	private final DatarouterPageHead head;
	private final DatarouterPageBody body;

	public DatarouterPage(
			DatarouterPageHead head,
			DatarouterPageBody body){
		this.head = head;
		this.body = body;
	}

	public ContainerTag<?> build(){
		return html(head.build(), body.build());
	}

}

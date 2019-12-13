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
package io.datarouter.web.html.j2html.bootstrap4;

import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.text;

import io.datarouter.web.handler.BaseHandler;
import j2html.tags.ContainerTag;

public class Bootstrap4FormTool{

	public static ContainerTag textField(String name, String display, String placeholder){
		var label = label(display)
				.withClass("form-label");
		var input = input()
				.withClass("form-control")
				.withName(name)
				.withPlaceholder(placeholder)
				.withType("text");
		return div(label, input)
				.withClass("form-group");
	}

	public static ContainerTag checkboxField(String name, String display){
		var input = input()
				.withClass("form-check-input")
				.withName(name)
				.withType("checkbox");
		var label = label(input, text(display))
				.withClass("form-check-label");
		return div(label)
				.withClass("form-group form-check");
	}

	public static ContainerTag submitButton(String value, String display){
		var button = button(display)
				.withClass("btn btn-success mx-1")
				.withName(BaseHandler.SUBMIT_ACTION)
				.withType("submit")
				.withValue(value);
		return div(button)
				.withClass("form-group");
	}

}

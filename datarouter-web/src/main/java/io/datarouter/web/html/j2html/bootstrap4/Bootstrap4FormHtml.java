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
import static j2html.TagCreator.each;
import static j2html.TagCreator.form;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.option;
import static j2html.TagCreator.select;
import static j2html.TagCreator.text;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.BaseHtmlFormField;
import io.datarouter.web.html.form.HtmlFormButton;
import io.datarouter.web.html.form.HtmlFormCheckbox;
import io.datarouter.web.html.form.HtmlFormEmail;
import io.datarouter.web.html.form.HtmlFormPassword;
import io.datarouter.web.html.form.HtmlFormSelect;
import io.datarouter.web.html.form.HtmlFormText;
import j2html.attributes.Attr;
import j2html.tags.ContainerTag;

public class Bootstrap4FormHtml{

	public static ContainerTag render(HtmlForm form){
		return form(each(form.getFields(), Bootstrap4FormHtml::renderField))
				.withAction(form.getAction())
				.withMethod(form.getMethod());
	}

	private static ContainerTag renderField(BaseHtmlFormField field){
		if(field instanceof HtmlFormButton){
			return submitButton((HtmlFormButton)field);
		}else if(field instanceof HtmlFormCheckbox){
			return checkboxField((HtmlFormCheckbox)field);
		}else if(field instanceof HtmlFormEmail){
			return emailField((HtmlFormEmail)field);
		}else if(field instanceof HtmlFormPassword){
			return passwordField((HtmlFormPassword)field);
		}else if(field instanceof HtmlFormSelect){
			return selectField((HtmlFormSelect)field);
		}else if(field instanceof HtmlFormText){
			return textField((HtmlFormText)field);
		}else{
			throw new IllegalArgumentException(field.getClass() + "is an unknown subclass of "
					+ BaseHtmlFormField.class);
		}
	}

	private static ContainerTag submitButton(HtmlFormButton field){
		var button = button(field.getDisplay())
				.withClass("btn btn-success mx-1")
				.withName(BaseHandler.SUBMIT_ACTION)
				.withType("submit")
				.withValue(field.getValue());
		return div(button)
				.withClass("form-group");
	}

	private static ContainerTag checkboxField(HtmlFormCheckbox field){
		var input = input()
				.withClass("form-check-input")
				.withName(field.getName())
				.withType("checkbox")
				.condAttr(field.isChecked(), Attr.CHECKED, null);
		var label = label(input, text(field.getDisplay()))
				.withClass("form-check-label");
		return div(label)
				.withClass("form-group form-check");
	}

	private static ContainerTag emailField(HtmlFormEmail field){
		var label = label(field.getDisplay())
				.withClass("form-label");
		var input = input()
				.withClass("form-control")
				.withName(field.getName())
				.withPlaceholder(field.getPlaceholder())
				.withType("email")
				.condAttr(field.isAutofocus(), "autofocus", null)
				.condAttr(field.isRequired(), "required", null);
		return div(label, input)
				.withClass("form-group");
	}

	private static ContainerTag passwordField(HtmlFormPassword field){
		var label = label(field.getDisplay())
				.withClass("form-label");
		var input = input()
				.withClass("form-control")
				.withName(field.getName())
				.withPlaceholder(field.getPlaceholder())
				.withType("password")
				.condAttr(field.isRequired(), "required", null);
		return div(label, input)
				.withClass("form-group");
	}

	private static ContainerTag selectField(HtmlFormSelect field){
		var label = label(field.getDisplay())
				.withClass("form-label");
		var select = select()
				.withClass("form-control")
				.withName(field.getName())
				.with(each(field.getDisplayByValue().entrySet(), entry -> option(entry.getValue()).withValue(entry
						.getKey())))
				.condAttr(field.isMultiple(), "multiple", null);
		return div(label, select)
				.withClass("form-group");
	}

	private static ContainerTag textField(HtmlFormText field){
		String inputClass = "form-control";
		if(field.getError() != null){
			inputClass += " is-invalid";
		}
		var label = label(field.getDisplay())
				.withClass("form-label");
		var input = input()
				.withClass(inputClass)
				.withName(field.getName())
				.withPlaceholder(field.getPlaceholder())
				.withType("text")
				.withValue(field.getValue());
		var error = field.getError() == null ? null : div(field.getError())
				.withClass("invalid-feedback");
		return div(label, input, error)
				.withClass("form-group");
	}

}

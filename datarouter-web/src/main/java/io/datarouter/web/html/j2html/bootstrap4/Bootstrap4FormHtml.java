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
package io.datarouter.web.html.j2html.bootstrap4;

import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.option;
import static j2html.TagCreator.select;
import static j2html.TagCreator.span;
import static j2html.TagCreator.text;
import static j2html.TagCreator.textarea;

import java.text.SimpleDateFormat;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.BaseHtmlFormField;
import io.datarouter.web.html.form.HtmlFormButton;
import io.datarouter.web.html.form.HtmlFormCheckbox;
import io.datarouter.web.html.form.HtmlFormDate;
import io.datarouter.web.html.form.HtmlFormDateTime;
import io.datarouter.web.html.form.HtmlFormEmail;
import io.datarouter.web.html.form.HtmlFormPassword;
import io.datarouter.web.html.form.HtmlFormSelect;
import io.datarouter.web.html.form.HtmlFormText;
import io.datarouter.web.html.form.HtmlFormTextArea;
import j2html.attributes.Attr;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.FormTag;
import j2html.tags.specialized.TextareaTag;

public class Bootstrap4FormHtml{

	private static final String LABEL_CLASS = "form-label mr-2";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static FormTag render(HtmlForm htmlForm){
		return render(htmlForm, false);
	}

	public static FormTag render(HtmlForm form, boolean inline){
		return form(renderFields(form))
				.withAction(form.getAction())
				.withCondClass(inline, "form-inline")
				.withMethod(form.getMethod());
	}

	private static DivTag[] renderFields(HtmlForm form){
		return form.getFields().stream()
				.map(Bootstrap4FormHtml::renderField)
				.toArray(DivTag[]::new);
	}

	private static DivTag renderField(BaseHtmlFormField field){
		DivTag div;
		if(field instanceof HtmlFormButton){
			div = submitButton((HtmlFormButton)field);
		}else if(field instanceof HtmlFormCheckbox){
			div = checkboxField((HtmlFormCheckbox)field);
		}else if(field instanceof HtmlFormEmail){
			div = emailField((HtmlFormEmail)field);
		}else if(field instanceof HtmlFormPassword){
			div = passwordField((HtmlFormPassword)field);
		}else if(field instanceof HtmlFormSelect){
			div = selectField((HtmlFormSelect)field);
		}else if(field instanceof HtmlFormText){
			div = textField((HtmlFormText)field);
		}else if(field instanceof HtmlFormTextArea){
			div = textField((HtmlFormTextArea)field);
		}else if(field instanceof HtmlFormDate){
			div = dateField((HtmlFormDate)field);
		}else if(field instanceof HtmlFormDateTime){
			div = dateTimeField((HtmlFormDateTime)field);
		}else{
			throw new IllegalArgumentException(field.getClass() + "is an unknown subclass of "
					+ BaseHtmlFormField.class);
		}
		return div(div)
				.withClass("mx-3");
	}

	private static DivTag submitButton(HtmlFormButton field){
		var button = button(field.getDisplay())
				.withClass("btn btn-success mx-1")
				.withName(BaseHandler.SUBMIT_ACTION)
				.withType("submit")
				.withValue(field.getValue());
		return div(button)
				.withClass("form-group");
	}

	private static DivTag checkboxField(HtmlFormCheckbox field){
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

	private static DivTag emailField(HtmlFormEmail field){
		var label = label(field.getDisplay())
				.withClass(LABEL_CLASS);
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

	private static DivTag passwordField(HtmlFormPassword field){
		var label = label(field.getDisplay())
				.withClass(LABEL_CLASS);
		var input = input()
				.withClass("form-control")
				.withName(field.getName())
				.withPlaceholder(field.getPlaceholder())
				.withType("password")
				.condAttr(field.isRequired(), "required", null);
		return div(label, input)
				.withClass("form-group");
	}

	private static DivTag selectField(HtmlFormSelect field){
		var label = label(field.getDisplay())
				.condWith(field.isRequired(), span("*").withClass("text-danger"))
				.withClass(LABEL_CLASS);
		var options = field.getDisplayByValue().entrySet().stream()
				.map(entry -> {
					var option = option(entry.getValue())
							.withValue(entry.getKey());
					if(field.isMultiple()){
						Scanner.of(field.getSelectedMultiple())
								.include(selected -> entry.getValue().equals(selected))
								.findFirst()
								.ifPresent($ -> option.attr(Attr.SELECTED));
					}else if(entry.getValue().equals(field.getSelected())){
						option.attr(Attr.SELECTED);
					}
					return option;
				})
				.toArray(ContainerTag[]::new);
		var select = select(options)
				.withClass("form-control")
				.withName(field.getName())
				.attr(Attr.SIZE, field.getSize())
				.condAttr(field.isMultiple(), "multiple", null)
				.condAttr(field.isRequired(), "required", null);
		return div(label, select)
				.withClass("form-group");
	}

	private static DivTag textField(HtmlFormText field){
		String inputClass = "form-control";
		if(field.getError() != null){
			inputClass += " is-invalid";
		}
		var label = label(text(field.getDisplay()))
				.condWith(field.isRequired(), span("*").withClass("text-danger"))
				.withClass(LABEL_CLASS);
		var input = input()
				.withClass(inputClass)
				.withName(field.getName())
				.withPlaceholder(field.getPlaceholder())
				.withType("text")
				.withCondReadonly(field.isReadOnly())
				.withValue(field.getValue())
				.condAttr(field.isRequired(), "required", null);
		var error = field.getError() == null ? null : div(field.getError())
				.withClass("invalid-feedback");
		return div(label, input, error)
				.withClass("form-group");
	}

	private static DivTag textField(HtmlFormTextArea field){
		String inputClass = "form-control";
		if(field.getError() != null){
			inputClass += " is-invalid";
		}
		var label = label(text(field.getDisplay()))
				.condWith(field.isRequired(), span("*").withClass("text-danger"))
				.withClass(LABEL_CLASS);
		TextareaTag input;
		if(field.getValue() != null){
			input = textarea(field.getValue());
		}else{
			input = textarea();
		}
		input
				.withClass(inputClass)
				.withName(field.getName())
				.withPlaceholder(field.getPlaceholder())
				.withCondReadonly(field.isReadOnly())
				.withCondPlaceholder(field.isReadOnly(), field.getValue())
				.condAttr(field.isRequired(), "required", null);
		if(field.getMaxLength() != null){
			input.attr(Attr.MAXLENGTH, field.getMaxLength());
		}
		if(field.getRows() != null){
			input.attr(Attr.ROWS, field.getRows());
		}
		var error = field.getError() == null ? null : div(field.getError())
				.withClass("invalid-feedback");
		return div(label, input, error)
				.withClass("form-group");
	}

	private static DivTag dateField(HtmlFormDate field){
		String inputClass = "form-control";
		if(field.getError() != null){
			inputClass += " is-invalid";
		}
		var label = label(text(field.getDisplay()))
				.condWith(field.isRequired(), span("*").withClass("text-danger"))
				.withClass(LABEL_CLASS);
		var input = input()
				.withClass(inputClass)
				.withName(field.getName())
				.withPlaceholder(field.getPlaceholder())
				.withType("date")
				.withValue(field.getValue())
				.condAttr(field.isRequired(), "required", null);
		var error = field.getError() == null ? null : div(field.getError())
				.withClass("invalid-feedback");
		return div(label, input, error)
				.withClass("form-group");
	}

	private static DivTag dateTimeField(HtmlFormDateTime field){
		String inputClass = "form-control";
		if(field.getError() != null){
			inputClass += " is-invalid";
		}
		var label = label(text(field.getDisplay()))
				.condWith(field.isRequired(), span("*").withClass("text-danger"))
				.withClass(LABEL_CLASS);
		var input = input()
				.withClass(inputClass)
				.withName(field.getName())
				.withPlaceholder(field.getPlaceholder())
				.withType("datetime-local")
				.withValue(field.getValue())
				.condAttr(field.isRequired(), "required", null);
		var error = field.getError() == null ? null : div(field.getError())
				.withClass("invalid-feedback");
		return div(label, input, error)
				.withClass("form-group");
	}

}

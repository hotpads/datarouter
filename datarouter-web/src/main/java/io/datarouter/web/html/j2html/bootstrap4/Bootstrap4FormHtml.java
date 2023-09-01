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
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.textarea;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.html.form.BaseHtmlFormField;
import io.datarouter.web.html.form.BaseHtmlFormSubmitInputField;
import io.datarouter.web.html.form.BaseHtmlFormSubmitInputField.HtmlFormButtonSize;
import io.datarouter.web.html.form.BaseHtmlFormSubmitInputField.HtmlFormButtonStyle;
import io.datarouter.web.html.form.BaseHtmlFormTypedInputField;
import io.datarouter.web.html.form.BaseHtmlLabeledFormField;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlFormCheckbox;
import io.datarouter.web.html.form.HtmlFormCheckboxTable;
import io.datarouter.web.html.form.HtmlFormCheckboxTable.Column;
import io.datarouter.web.html.form.HtmlFormSelect;
import io.datarouter.web.html.form.HtmlFormTextArea;
import j2html.TagCreator;
import j2html.attributes.Attr;
import j2html.tags.DomContent;
import j2html.tags.specialized.ButtonTag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.FormTag;
import j2html.tags.specialized.InputTag;
import j2html.tags.specialized.LabelTag;
import j2html.tags.specialized.SelectTag;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TextareaTag;

public class Bootstrap4FormHtml{

	private static final String FORM_GROUP_CLASS = "form-group";
	private static final String LABEL_CLASS = "form-label mr-2";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static FormTag render(HtmlForm htmlForm){
		return render(htmlForm, false);
	}

	public static FormTag render(HtmlForm form, boolean inline){
		return form()
				.withId(form.getId())
				.withAction(form.getAction())
				.withCondClass(inline, "form-inline")
				.withMethod(form.getMethod().method)
				.condWith(form.getError() != null, div(form.getError())
						.withClasses("alert", "alert-danger")
						.attr(Attr.ROLE, "alert"))
				.with(renderFields(form))
				.with(form.getHiddenFields().stream()
						.map(field -> input()
								.withType("hidden")
								.withName(field.name())
								.withValue(field.value())));
	}

	private static DivTag[] renderFields(HtmlForm form){
		return form.getFields().stream()
				.map(Bootstrap4FormHtml::renderField)
				.toArray(DivTag[]::new);
	}

	private static DivTag renderField(BaseHtmlFormField<?> baseField){
		DomContent rendered;
		if(baseField instanceof HtmlFormCheckbox field){
			rendered = checkboxField(field);
		}else if(baseField instanceof HtmlFormCheckboxTable field){
			rendered = checkboxTableField(field);
		}else if(baseField instanceof HtmlFormTextArea field){
			rendered = textareaField(field);
		}else if(baseField instanceof HtmlFormSelect field){
			rendered = selectField(field);
		}else if(baseField instanceof BaseHtmlFormSubmitInputField field){
			rendered = submitButton(field);
		}else if(baseField instanceof BaseHtmlFormTypedInputField field){
			rendered = inputAndLabel(field);
		}else{
			throw new IllegalArgumentException(baseField.getClass() + "is an unknown subclass of "
					+ BaseHtmlFormField.class);
		}
		return div(rendered)
				.condWith(baseField.getError() != null, div(baseField.getError()).withClass("invalid-feedback"))
				.withClass("mx-3");
	}

	private static DivTag checkboxTableField(HtmlFormCheckboxTable field){
		TableTag table = table()
				.withId(field.getId())
				.withClass("table table-hover bg-white")
				.with(thead(tr()
						.with(field.getColumns().stream()
								.map(Column::display)
								.map(TagCreator::th))))
				.with(Scanner.of(field.getRows())
						.map(row -> tr()
								.with(td(input()
											.withType("checkbox")
											.withName(row.name())
											.withCondChecked(row.checked())
											.withCondDisabled(row.disabled())))
								.with(row.values().stream().map(TagCreator::td)))
						.list());
		return fieldAndLabel(field, table);
	}

	private static DivTag selectField(HtmlFormSelect field){
		SelectTag select = select()
				.with(field.getDisplayByValue().entrySet().stream()
						.map(entry -> option(entry.getValue())
								.withValue(entry.getKey())
								.withCondSelected(field.getSelected().contains(entry.getKey()))))
				.withClasses(getInputClasses(field))
				.withCondName(field.getName() != null, field.getName())
				.withCondSize(field.getSize() != null, Objects.toString(field.getSize()))
				.withCondMultiple(field.isMultiple())
				.withCondRequired(field.isRequired())
				.withCondDisabled(field.isDisabled())
				.withCondAutofocus(field.isAutofocus())
				.condAttr(field.isSubmitOnChange(), "onchange", "this.form.submit()");
		return fieldAndLabel(field, select);
	}

	private static DivTag fieldAndLabel(BaseHtmlLabeledFormField<?> field, DomContent content){
		return div(makeLabel(field), content)
				.withClass(FORM_GROUP_CLASS);
	}

	private static DivTag inputAndLabel(BaseHtmlFormTypedInputField<?> field){
		return fieldAndLabel(field, inputField(field).withClasses(getInputClasses(field)));
	}

	private static DivTag textareaField(HtmlFormTextArea field){
		TextareaTag textarea = textarea()
				.withId(field.getId())
				.withClasses(getInputClasses(field))
				.withCondName(field.getName() != null, field.getName())
				.condWith(field.getValue() != null, text(field.getValue()))
				.withCondPlaceholder(field.getPlaceholder() != null, field.getPlaceholder())
				.withCondMaxlength(field.getMaxLength() != null, Objects.toString(field.getMaxLength()))
				.withCondDisabled(field.isDisabled())
				.withCondAutofocus(field.isAutofocus())
				.withCondReadonly(field.isReadonly())
				.withCondRequired(field.isRequired())
				.condAttr(field.isSubmitOnChange(), "onchange", "this.form.submit()")
				.withCondRows(field.getRows() != null, Objects.toString(field.getRows()));
		return fieldAndLabel(field, textarea);
	}

	private static ButtonTag submitButton(BaseHtmlFormSubmitInputField<?> field){
		return button(field.getLabel())
				.withId(field.getId())
				.withType("submit")
				.withClasses(getInputClasses(field))
				.withCondName(field.getName() != null, field.getName())
				.withCondValue(field.getValue() != null, field.getValue())
				.withCondDisabled(field.isDisabled())
				.withCondAutofocus(field.isAutofocus())
				.condAttr(field.isSubmitOnChange(), "onchange", "this.form.submit()")
				.withClasses(
						"btn",
						getButtonStyleClass(field.getStyle()),
						getButtonStyleSize(field.getElementSize()),
						"mx-1")
				.condAttr(field.getOnClickConfirmText() != null, "onclick",
						"return confirm('" + field.getOnClickConfirmText() + "')");
	}

	private static DivTag checkboxField(HtmlFormCheckbox field){
		LabelTag label = label()
				.with(inputField(field)
						.withCondChecked(field.isChecked())
						.withClass("form-check-input"))
				.withText(field.getLabel())
				.withClasses("form-check-label");

		return div(label)
				.withClasses(FORM_GROUP_CLASS, "form-check");
	}

	private static InputTag inputField(BaseHtmlFormTypedInputField<?> field){
		return input()
				.withId(field.getId())
				.withType(field.getType())
				.withCondName(field.getName() != null, field.getName())
				.withCondValue(field.getValue() != null, field.getValue())
				.withCondSize(field.getSize() != null, Objects.toString(field.getSize()))
				.withCondPlaceholder(field.getPlaceholder() != null, field.getPlaceholder())
				.withCondMaxlength(field.getMaxLength() != null, Objects.toString(field.getMaxLength()))
				.withCondDisabled(field.isDisabled())
				.withCondAutofocus(field.isAutofocus())
				.withCondReadonly(field.isReadonly())
				.withCondRequired(field.isRequired())
				.condAttr(field.isSubmitOnChange(), "onchange", "this.form.submit()");
	}

	private static String getButtonStyleClass(HtmlFormButtonStyle style){
		return switch(style){
		case PRIMARY -> "btn-primary";
		case SECONDARY -> "btn-secondary";
		case SUCCESS -> "btn-success";
		case WARNING -> "btn-warning";
		case DANGER -> "btn-danger";
		case INFO -> "btn-info";
		};
	}

	private static String getButtonStyleSize(HtmlFormButtonSize size){
		return switch(size){
		case SMALL -> "btn-sm";
		case DEFAULT -> null;
		case LARGE -> "btn-lg";
		};
	}

	private static String[] getInputClasses(BaseHtmlFormField<?> field){
		return field.getError() == null
				? new String[]{"form-control"}
				: new String[]{"form-control", "is-invalid"};
	}

	private static LabelTag makeLabel(BaseHtmlLabeledFormField<?> field){
		return label(field.getLabel())
				.condWith(field.isRequired(), span("*").withClass("text-danger"))
				.withClass(LABEL_CLASS);
	}

	public static long parseDateFieldToEpochMillis(String dateString){
		try{
			return DATE_FORMAT.parse(dateString).getTime();
		}catch(ParseException e){
			String message = String.format("invalid input=%s", dateString);
			throw new IllegalArgumentException(message, e);
		}
	}

	private record ContentAndClasses(
			List<DomContent> content,
			List<String> containerClasses){
	}

}

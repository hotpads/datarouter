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
package io.datarouter.web.html.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HtmlForm{

	public abstract static class BaseHtmlFormField{

		protected String error;

		public BaseHtmlFormField(){
		}

		public String getError(){
			return error;
		}

		public boolean hasError(){
			return error != null;
		}

	}

	public record HtmlFormHiddenField(
			String name,
			String value){
	}

	private String action;
	private String method;
	private List<BaseHtmlFormField> fields = new ArrayList<>();
	private List<HtmlFormHiddenField> hiddenFields = new ArrayList<>();

	public HtmlForm withAction(String action){
		this.action = action;
		return this;
	}

	public HtmlForm withMethod(String method){
		this.method = method;
		return this;
	}

	public HtmlForm withMethodGet(){
		return withMethod("GET");
	}

	public HtmlForm withMethodPost(){
		return withMethod("POST");
	}

	public HtmlForm addField(BaseHtmlFormField field){
		this.fields.add(field);
		return this;
	}

	public HtmlForm addFields(Collection<BaseHtmlFormField> fields){
		this.fields.addAll(fields);
		return this;
	}

	public HtmlForm addFields(BaseHtmlFormField... fields){
		this.fields.addAll(List.of(fields));
		return this;
	}

	public HtmlFormCheckbox addCheckboxField(){
		var field = new HtmlFormCheckbox();
		fields.add(field);
		return field;
	}

	public HtmlFormButton addButton(){
		var field = new HtmlFormButton();
		fields.add(field);
		return field;
	}

	public HtmlFormButtonWithoutSubmitAction addButtonWithoutSubmitAction(){
		var field = new HtmlFormButtonWithoutSubmitAction();
		fields.add(field);
		return field;
	}

	public HtmlFormEmail addEmailField(){
		var field = new HtmlFormEmail();
		fields.add(field);
		return field;
	}

	public HtmlFormPassword addPasswordField(){
		var field = new HtmlFormPassword();
		fields.add(field);
		return field;
	}

	public HtmlFormSelect addSelectField(){
		var field = new HtmlFormSelect();
		fields.add(field);
		return field;
	}

	public HtmlFormTimezoneSelect addTimezoneSelectField(){
		var field = new HtmlFormTimezoneSelect();
		fields.add(field);
		return field;
	}

	public HtmlFormText addTextField(){
		var field = new HtmlFormText();
		fields.add(field);
		return field;
	}

	public HtmlFormTextArea addTextAreaField(){
		var field = new HtmlFormTextArea();
		fields.add(field);
		return field;
	}

	public HtmlFormDate addDateField(){
		var field = new HtmlFormDate();
		fields.add(field);
		return field;
	}

	public HtmlFormDateTime addDateTimeField(){
		var field = new HtmlFormDateTime();
		fields.add(field);
		return field;
	}

	public HtmlForm addHiddenField(String name, String value){
		return addHiddenField(new HtmlFormHiddenField(name, value));
	}

	public HtmlForm addHiddenField(HtmlFormHiddenField hiddenField){
		this.hiddenFields.add(hiddenField);
		return this;
	}

	public HtmlForm addHiddenFields(List<HtmlFormHiddenField> hiddenFields){
		hiddenFields.forEach(this::addHiddenField);
		return this;
	}

	public HtmlForm addHiddenFields(HtmlFormHiddenField... hiddenFields){
		Arrays.asList(hiddenFields).forEach(this::addHiddenField);
		return this;
	}

	public String getAction(){
		return action;
	}

	public String getMethod(){
		return method;
	}

	public List<BaseHtmlFormField> getFields(){
		return fields;
	}

	public List<HtmlFormHiddenField> getHiddenFields(){
		return hiddenFields;
	}

	public boolean hasErrors(){
		return fields.stream()
				.anyMatch(BaseHtmlFormField::hasError);
	}

}

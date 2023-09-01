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

public class HtmlForm extends BaseHtmlFormField<HtmlForm>{

	private String action;
	private final HtmlFormMethod method;
	private final List<BaseHtmlFormField<?>> fields = new ArrayList<>();
	private final List<HtmlFormHiddenField> hiddenFields = new ArrayList<>();

	public HtmlForm(HtmlFormMethod method){
		this.method = method;
	}

	public HtmlForm withAction(String action){
		this.action = action;
		return this;
	}

	public HtmlForm addField(BaseHtmlFormField<?> field){
		this.fields.add(field);
		return this;
	}

	public HtmlForm addFields(Collection<BaseHtmlFormField<?>> fields){
		this.fields.addAll(fields);
		return this;
	}

	public HtmlForm addFields(BaseHtmlFormField<?>... fields){
		this.fields.addAll(List.of(fields));
		return this;
	}

	public HtmlFormCheckbox addCheckboxField(){
		var field = new HtmlFormCheckbox();
		fields.add(field);
		return field;
	}

	public HtmlFormCheckboxTable addCheckboxTableField(){
		var field = new HtmlFormCheckboxTable();
		fields.add(field);
		return field;
	}

	public HtmlFormSubmitActionButton addButton(){
		var field = new HtmlFormSubmitActionButton();
		fields.add(field);
		return field;
	}

	public HtmlFormSubmitWithoutSubmitActionButton addButtonWithoutSubmitAction(){
		var field = new HtmlFormSubmitWithoutSubmitActionButton();
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

	public HtmlFormNumber addNumberField(){
		var field = new HtmlFormNumber();
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

	public HtmlFormMethod getMethod(){
		return method;
	}

	public List<BaseHtmlFormField<?>> getFields(){
		return fields;
	}

	public List<HtmlFormHiddenField> getHiddenFields(){
		return hiddenFields;
	}

	public boolean hasErrors(){
		return fields.stream()
				.anyMatch(field -> field.getError() != null);
	}

	@Override
	protected HtmlForm self(){
		return this;
	}

	public record HtmlFormHiddenField(
			String name,
			String value){
	}

	public enum HtmlFormMethod{
		GET("get"),
		POST("post");

		public final String method;

		HtmlFormMethod(String method){
			this.method = method;
		}

	}

}

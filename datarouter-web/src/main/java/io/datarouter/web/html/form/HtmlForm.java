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

	private String action;
	private String method;
	private List<BaseHtmlFormField> fields = new ArrayList<>();

	public HtmlForm withAction(String action){
		this.action = action;
		return this;
	}

	public HtmlForm withMethod(String method){
		this.method = method;
		return this;
	}

	public HtmlForm addFields(Collection<BaseHtmlFormField> fields){
		this.fields.addAll(fields);
		return this;
	}

	public HtmlForm addFields(BaseHtmlFormField... fields){
		this.fields.addAll(Arrays.asList(fields));
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

	public HtmlFormText addTextField(){
		var field = new HtmlFormText();
		fields.add(field);
		return field;
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

	public boolean hasErrors(){
		return fields.stream()
				.filter(BaseHtmlFormField::hasError)
				.findAny()
				.isPresent();
	}

}

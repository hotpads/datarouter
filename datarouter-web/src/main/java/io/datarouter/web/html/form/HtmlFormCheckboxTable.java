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

import java.util.List;

public class HtmlFormCheckboxTable extends BaseHtmlLabeledFormField<HtmlFormCheckboxTable>{

	private static final String COLLECT_VALUES_JS = """
			document.addEventListener("DOMContentLoaded", function(){
				const formEl = document.getElementById("%s");
				formEl.addEventListener("submit", function(){
					const table = document.getElementById("%s");
					const checkboxes = Array.from(table.querySelectorAll("input[type=checkbox]"));
					const requestedRoles = checkboxes.filter(checkbox => checkbox.checked && !checkbox.disabled)
							.map(checkbox => checkbox.name);
					const hiddenField = document.getElementsByName("%s")[0];
					hiddenField.value = requestedRoles;
				});
			});""";

	private List<Column> columns;
	private List<Row> rows;

	public HtmlFormCheckboxTable withColumns(List<Column> columns){
		this.columns = columns;
		return this;
	}

	public HtmlFormCheckboxTable withRows(List<Row> rows){
		this.rows = rows;
		return this;
	}

	public List<Column> getColumns(){
		return columns;
	}

	public List<Row> getRows(){
		return rows;
	}

	public String getCollectValuesJs(String formId, String tableId, String hiddenFieldName){
		return COLLECT_VALUES_JS.formatted(formId, tableId, hiddenFieldName);
	}

	@Override
	protected HtmlFormCheckboxTable self(){
		return this;
	}

	public record Column(
			String name,
			String display){
	}

	public record Row(
			String name,
			List<String> values,
			boolean disabled,
			boolean checked){
	}

}

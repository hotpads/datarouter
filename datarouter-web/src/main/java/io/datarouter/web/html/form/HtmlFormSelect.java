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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.web.html.form.HtmlForm.BaseHtmlFormField;

public class HtmlFormSelect extends BaseHtmlFormField{

	private String name;
	private String display;
	private Map<String,String> displayByValue = new LinkedHashMap<>();
	private boolean multiple;
	private String selected;
	private List<String> selectedMultiple = new ArrayList<>();
	private boolean required;
	private int size;
	private boolean submitOnChange;

	public HtmlFormSelect withName(String name){
		this.name = name;
		return this;
	}

	public HtmlFormSelect withDisplay(String display){
		this.display = display;
		return this;
	}

	public HtmlFormSelect withDisplayByValue(Map<String,String> displayByValue){
		this.displayByValue = displayByValue;
		return this;
	}

	//display the raw values
	public HtmlFormSelect withValues(List<String> values){
		this.displayByValue = values.stream()
				.collect(Collectors.toMap(
						Function.identity(),
						Function.identity(),
						(a, b) -> a,
						LinkedHashMap::new));
		return this;
	}

	public HtmlFormSelect multiple(){
		this.multiple = true;
		return this;
	}

	public HtmlFormSelect withSelected(String selected){
		this.selected = selected;
		return this;
	}

	public HtmlFormSelect withSelectedMultiple(List<String> selected){
		this.selectedMultiple = selected;
		return this;
	}

	public HtmlFormSelect required(){
		this.required = true;
		return this;
	}

	public HtmlFormSelect withSize(int size){
		this.size = size;
		return this;
	}

	public HtmlFormSelect withSubmitOnChange(){
		this.submitOnChange = true;
		return this;
	}

	public String getName(){
		return name;
	}

	public String getDisplay(){
		return display;
	}

	public Map<String,String> getDisplayByValue(){
		return displayByValue;
	}

	public boolean isMultiple(){
		return multiple;
	}

	public String getSelected(){
		return selected;
	}

	public List<String> getSelectedMultiple(){
		return selectedMultiple;
	}

	public boolean isRequired(){
		return required;
	}

	public String getSize(){
		return Integer.toString(size);
	}

	public boolean isSubmitOnChange(){
		return submitOnChange;
	}

}

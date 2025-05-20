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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HtmlFormSelect extends BaseHtmlFormTextField<HtmlFormSelect>{

	private Map<String,String> displayByValue = new LinkedHashMap<>();
	private List<String> selected = new ArrayList<>();
	private boolean multiple;
	private int size;

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
						(a, _) -> a,
						LinkedHashMap::new));
		return this;
	}

	public boolean isMultiple(){
		return multiple;
	}

	public HtmlFormSelect multiple(){
		this.multiple = true;
		return self();
	}

	public HtmlFormSelect withSelected(String selected){
		return withSelected(Optional.ofNullable(selected).map(List::of).orElseGet(List::of));
	}

	public HtmlFormSelect withSelected(List<String> selected){
		this.selected = selected;
		return self();
	}

	public List<String> getSelected(){
		return selected;
	}

	public Integer getSize(){
		return size;
	}

	public HtmlFormSelect withSize(int size){
		this.size = size;
		return self();
	}

	public Map<String,String> getDisplayByValue(){
		return displayByValue;
	}

	@Override
	protected HtmlFormSelect self(){
		return this;
	}

}

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

import io.datarouter.web.html.form.HtmlForm.BaseHtmlFormField;

public class HtmlFormButton extends BaseHtmlFormField{

	private String value;
	private String display;
	private String clazz;
	private String onClickConfirmText;

	public HtmlFormButton withValue(String value){
		this.value = value;
		return this;
	}

	public HtmlFormButton withDisplay(String display){
		this.display = display;
		return this;
	}

	public HtmlFormButton withClass(String clazz){
		this.clazz = clazz;
		return this;
	}

	public HtmlFormButton withOnClickConfirmText(String onClickConfirmText){
		this.onClickConfirmText = onClickConfirmText;
		return this;
	}

	public String getValue(){
		return value;
	}

	public String getDisplay(){
		return display;
	}

	public String getOnClickConfirmText(){
		return onClickConfirmText;
	}

	public String getClazz(){
		return clazz;
	}

}

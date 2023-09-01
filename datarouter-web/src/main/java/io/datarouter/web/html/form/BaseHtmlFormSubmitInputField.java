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

public abstract class BaseHtmlFormSubmitInputField<T> extends BaseHtmlFormTextField<T>{

	private HtmlFormButtonStyle style = HtmlFormButtonStyle.PRIMARY;
	private HtmlFormButtonSize size = HtmlFormButtonSize.DEFAULT;
	private String onClickConfirmText;

	public T withOnClickConfirmText(String onClickConfirmText){
		this.onClickConfirmText = onClickConfirmText;
		return self();
	}

	public String getOnClickConfirmText(){
		return onClickConfirmText;
	}

	public T withSize(HtmlFormButtonSize size){
		this.size = size;
		return self();
	}

	public HtmlFormButtonSize getElementSize(){
		return size;
	}

	public T withStyle(HtmlFormButtonStyle style){
		this.style = style;
		return self();
	}

	public HtmlFormButtonStyle getStyle(){
		return style;
	}

	public enum HtmlFormButtonSize{
		SMALL,
		DEFAULT,
		LARGE,
	}

	public enum HtmlFormButtonStyle{
		PRIMARY,
		SECONDARY,
		SUCCESS,
		DANGER,
		WARNING,
		INFO
	}

}

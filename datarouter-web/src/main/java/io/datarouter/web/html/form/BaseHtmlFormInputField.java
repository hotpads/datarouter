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

public abstract class BaseHtmlFormInputField<T> extends BaseHtmlFormTextField<T>{

	private String placeholder;
	private Integer maxLength;
	private boolean readonly;

	public String getPlaceholder(){
		return placeholder;
	}

	public T withPlaceholder(String placeholder){
		this.placeholder = placeholder;
		return self();
	}

	public Integer getMaxLength(){
		return maxLength;
	}

	public T withMaxLength(Integer maxLength){
		this.maxLength = maxLength;
		return self();
	}

	public boolean isReadonly(){
		return readonly;
	}

	public T readonly(){
		return readonly(true);
	}

	public T readonly(boolean readonly){
		this.readonly = readonly;
		return self();
	}

}

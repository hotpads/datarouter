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

public abstract class BaseHtmlLabeledFormField<T> extends BaseHtmlFormField<T>{

	private String label;
	private boolean required;

	public String getLabel(){
		return label;
	}

	public T withLabel(String label){
		this.label = label;
		return self();
	}

	public boolean isRequired(){
		return required;
	}

	public T required(){
		return required(true);
	}

	public T required(boolean required){
		this.required = required;
		return self();
	}

}

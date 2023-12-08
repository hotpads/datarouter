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
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;

public abstract class BaseHtmlFormTextField<T> extends BaseHtmlLabeledFormField<T>{

	private String name;
	private boolean autofocus;
	private boolean disabled;
	private String value;
	private boolean submitOnChange;

	public String getName(){
		return name;
	}

	public T withName(String name){
		this.name = name;
		return self();
	}

	public boolean isAutofocus(){
		return autofocus;
	}

	public T autofocus(){
		return autofocus(true);
	}

	public T autofocus(boolean autofocus){
		this.autofocus = autofocus;
		return self();
	}

	public boolean isDisabled(){
		return disabled;
	}

	public T disabled(){
		return disabled(true);
	}

	public T disabled(boolean disabled){
		this.disabled = disabled;
		return self();
	}

	public String getValue(){
		return value;
	}

	public T withValue(String value){
		this.value = value;
		return self();
	}

	public T withValue(
			String value,
			boolean shouldValidate,
			Function<String,Optional<String>> errorFinder){
		return withValue(
				value,
				shouldValidate,
				List.of(errorFinder));
	}

	public T withValue(
			String value,
			boolean shouldValidate,
			List<Function<String,Optional<String>>> errorFinders){
		if(shouldValidate){
			Scanner.of(errorFinders)
					.concatOpt(fn -> fn.apply(value))
					.findFirst()
					.ifPresent(this::withError);
		}
		return withValue(value);
	}

	public T withSubmitOnChange(){
		this.submitOnChange = true;
		return self();
	}

	public boolean isSubmitOnChange(){
		return submitOnChange;
	}

}

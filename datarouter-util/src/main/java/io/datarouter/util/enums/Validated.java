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
package io.datarouter.util.enums;

import java.util.ArrayList;
import java.util.List;

public class Validated<T>{

	private List<String> errors = new ArrayList<>();
	private T value;

	public Validated(){
	}

	public Validated(T value){
		set(value);
	}

	public T get(){
		return value;
	}

	public void set(T value){
		this.value = value;
	}

	public Validated<T> addError(String errorMessage){
		if(errorMessage != null && !errorMessage.isEmpty()){
			errors.add(errorMessage);
		}
		return this;
	}

	public boolean isValid(){
		return errors.isEmpty();
	}

	public boolean hasErrors(){
		return !errors.isEmpty();
	}

	public String getErrorMessage(){
		return isValid() ? null : String.join(";", errors);
	}

	public List<String> getErrors(){
		return errors;
	}

}

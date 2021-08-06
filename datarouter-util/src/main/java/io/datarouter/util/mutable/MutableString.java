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
package io.datarouter.util.mutable;

/**
 * An object that contains a string, but remains the same object when the string changes.
 */
public class MutableString implements Comparable<MutableString>{

	private String string;

	public MutableString(String string){
		this.string = string;
	}

	@Override
	public boolean equals(Object object){
		if(object instanceof MutableString){
			return string.equals(((MutableString)object).getString());
		}
		return false;
	}

	@Override
	public int hashCode(){
		return string.hashCode();
	}

	@Override
	public String toString(){
		return getString();
	}

	@Override
	public int compareTo(MutableString otherString){
		return string.compareTo(otherString.string);
	}

	public void set(String string){
		this.string = string;
	}

	public String getString(){
		return string;
	}

}

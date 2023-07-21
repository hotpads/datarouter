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
package io.datarouter.bytes;

//TODO convert to record
public class LengthAndValue<T>{

	public final int length;
	public final T value;

	public LengthAndValue(int length, T value){
		this.length = length;
		this.value = value;
	}

	public LengthAndValue<T> assertLengthEquals(int expectedLength){
		if(expectedLength != length){
			String message = String.format("expectedLength=%s != actualLength=%s", expectedLength, length);
			throw new IllegalArgumentException(message);
		}
		return this;
	}

	public int length(){
		return length;
	}

	public T value(){
		return value;
	}

}

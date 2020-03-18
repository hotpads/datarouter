/**
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
package io.datarouter.scanner;

public class ObjectScanner<T> implements Scanner<T>{

	private final T object;
	private boolean advanced;

	public ObjectScanner(T object){
		this.object = object;
		this.advanced = false;
	}

	@Override
	public boolean advance(){
		if(advanced){
			return false;
		}
		advanced = true;
		return true;
	}

	@Override
	public T current(){
		return object;
	}

}

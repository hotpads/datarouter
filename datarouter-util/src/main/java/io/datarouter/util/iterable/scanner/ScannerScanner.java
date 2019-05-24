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
package io.datarouter.util.iterable.scanner;

public class ScannerScanner<T> extends BaseScanner<Scanner<T>>{

	private final Scanner<Scanner<T>> input;

	public ScannerScanner(Scanner<Scanner<T>> input){
		this.input = input;
	}

	@Override
	public boolean advance(){
		if(input.advance()){
			current = input.getCurrent();
			return true;
		}
		current = null;
		return false;
	}

	public Scanner<T> concatenate(){
		return new ConcatenatingScanner<>(this);
	}

}

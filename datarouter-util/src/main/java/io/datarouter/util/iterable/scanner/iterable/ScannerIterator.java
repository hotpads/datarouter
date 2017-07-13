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
package io.datarouter.util.iterable.scanner.iterable;

import java.util.Iterator;

import io.datarouter.util.exception.NotImplementedException;
import io.datarouter.util.iterable.scanner.Scanner;

public class ScannerIterator<T> implements Iterator<T>{

	private final Scanner<T> scanner;
	private T peeked;

	public ScannerIterator(Scanner<T> scanner){
		this.scanner = scanner;
	}


	@Override
	public boolean hasNext(){
		if(peeked != null){
			return true;
		}
		if(!scanner.advance()){
			return false;
		}
		peeked = scanner.getCurrent();
		return true;
	}

	@Override
	public T next(){
		if(!hasNext()){
			return null;
		}
		T ret = peeked;
		if(scanner.advance()){
			peeked = scanner.getCurrent();
		}else{
			peeked = null;
		}
		return ret;

	}

	@Override
	public void remove(){
		throw new NotImplementedException();
	}

}

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

import java.util.Iterator;

public class ScannerIterator<T> implements Iterator<T>{

	private final Scanner<T> scanner;

	private boolean isPeeked;
	private T peekedItem;

	public ScannerIterator(Scanner<T> scanner){
		this.scanner = scanner;
		this.isPeeked = false;
		this.peekedItem = null;
	}

	@Override
	public boolean hasNext(){
		if(isPeeked){
			return true;
		}
		if(scanner.advance()){
			peekedItem = scanner.current();
			isPeeked = true;
			return true;
		}
		peekedItem = null;
		isPeeked = false;
		scanner.close();
		return false;
	}

	@Override
	public T next(){
		if(hasNext()){
			T item = peekedItem;
			peekedItem = null;
			isPeeked = false;
			return item;
		}
		return null;
	}

	@Override
	public void remove(){
		throw new UnsupportedOperationException();
	}

}

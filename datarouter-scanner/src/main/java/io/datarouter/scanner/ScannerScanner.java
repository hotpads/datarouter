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

import java.util.Comparator;
import java.util.List;

public class ScannerScanner<T> extends BaseLinkedScanner<Scanner<T>,Scanner<T>>{

	@SafeVarargs
	public static <T> ScannerScanner<T> of(Scanner<T>... scanners){
		return new ScannerScanner<>(Scanner.of(scanners));
	}

	public ScannerScanner(Scanner<Scanner<T>> input){
		super(input);
	}

	@Override
	public boolean advanceInternal(){
		if(input.advance()){
			current = input.current();
			return true;
		}
		current = null;
		return false;
	}

	@SuppressWarnings("unchecked")
	public Scanner<T> collate(){
		return collate((Comparator<? super T>)Comparator.naturalOrder());
	}

	/**
	 * The ordering of input scanner items needs to match this comparator.
	 */
	public Scanner<T> collate(Comparator<? super T> comparator){
		List<Scanner<T>> inputScannerList = input.list();
		if(inputScannerList.size() == 1){
			return inputScannerList.get(0);
		}
		return new CollatingScanner<>(inputScannerList, comparator);
	}

	public Scanner<T> concatenate(){
		return new ConcatenatingScanner<>(this);
	}

}

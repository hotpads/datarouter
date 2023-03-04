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
package io.datarouter.scanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortingScanner<T> extends BaseLinkedScanner<T,T>{

	private final Comparator<? super T> comparator;

	private boolean consumedInput;
	private Scanner<T> sortedScanner;

	public SortingScanner(Scanner<T> input, Comparator<? super T> comparator){
		super(input);
		this.comparator = comparator;
		this.consumedInput = false;
	}

	@Override
	protected boolean advanceInternal(){
		if(!consumedInput){
			List<T> items = input.collect(ArrayList::new);
			items.sort(comparator);
			sortedScanner = Scanner.of(items);
			consumedInput = true;
		}
		if(sortedScanner.advance()){
			current = sortedScanner.current();
			return true;
		}
		return false;
	}

}

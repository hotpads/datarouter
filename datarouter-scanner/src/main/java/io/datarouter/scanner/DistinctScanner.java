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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class DistinctScanner<T,R> extends BaseLinkedScanner<T,T>{

	private final Function<T,R> mapper;
	private final Set<R> items;

	public DistinctScanner(Scanner<T> input, Function<T,R> mapper){
		super(input);
		this.mapper = mapper;
		this.items = new HashSet<>();
	}

	@Override
	protected boolean advanceInternal(){
		while(input.advance()){
			T item = input.current();
			if(items.add(mapper.apply(item))){
				current = item;
				return true;
			}
		}
		return false;
	}

}

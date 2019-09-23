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

public class DistinctScanner<T> extends BaseLinkedScanner<T,T>{

	private final Set<T> items;

	public DistinctScanner(Scanner<T> input){
		super(input);
		this.items = new HashSet<>();
	}

	@Override
	protected boolean advanceInternal(){
		while(input.advance()){
			T item = input.current();
			if(items.add(item)){
				current = item;
				return true;
			}
		}
		return false;
	}

}

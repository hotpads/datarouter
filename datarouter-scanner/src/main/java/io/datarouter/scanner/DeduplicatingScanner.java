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

import java.util.function.Function;

/**
 * Removes consecutive duplicates.  Lighter weight than Stream's distinct() because all elements need not be
 * collected into memory.
 */
public class DeduplicatingScanner<T> extends BaseLinkedScanner<T,T>{

	private final Function<T,?> keyMapper;

	public DeduplicatingScanner(Scanner<T> input, Function<T,?> keyMapper){
		super(input);
		this.keyMapper = keyMapper;
	}

	@Override
	public boolean advanceInternal(){
		while(input.advance()){
			if(current == null || !keyMapper.apply(input.current()).equals(keyMapper.apply(current))){
				current = input.current();
				return true;
			}
		}
		return false;
	}

}

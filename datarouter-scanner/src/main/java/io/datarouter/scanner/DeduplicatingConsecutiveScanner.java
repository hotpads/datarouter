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

import java.util.function.BiPredicate;
import java.util.function.Function;

public class DeduplicatingConsecutiveScanner<T,R> extends BaseLinkedScanner<T,T>{

	private final Function<T,R> mapper;
	private final BiPredicate<R,R> equalsPredicate;
	private boolean hasSetCurrent = false;

	public DeduplicatingConsecutiveScanner(
			Scanner<T> input,
			Function<T,R> mapper,
			BiPredicate<R,R> equalsPredicate){
		super(input);
		this.mapper = mapper;
		this.equalsPredicate = equalsPredicate;
	}

	@Override
	public boolean advanceInternal(){
		while(input.advance()){
			if(!hasSetCurrent || !equalsPredicate.test(mapper.apply(current), mapper.apply(input.current()))){
				current = input.current();
				hasSetCurrent = true;
				return true;
			}
		}
		return false;
	}

}

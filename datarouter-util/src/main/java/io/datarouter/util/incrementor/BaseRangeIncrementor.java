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
package io.datarouter.util.incrementor;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;

public abstract class BaseRangeIncrementor<T extends Comparable<? super T>> implements Scanner<T>{

	private final Range<T> range;
	private boolean pendingInit;
	private T current;

	public BaseRangeIncrementor(Range<T> range){
		this.range = range;
		this.pendingInit = true;
	}

	protected abstract T increment(T item);

	@Override
	public boolean advance(){
		if(pendingInit){
			current = range.getStartInclusive() ? range.getStart() : increment(range.getStart());
			pendingInit = false;
		}else{
			current = increment(current);
		}
		return range.contains(current);
	}

	@Override
	public T current(){
		return current;
	}

}

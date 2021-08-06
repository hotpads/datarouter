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

import java.util.Comparator;
import java.util.Objects;

public class ComparableScanner<T> extends BaseLinkedScanner<T,T> implements Comparable<Scanner<T>>{

	private final Comparator<? super T> comparator;

	public ComparableScanner(Scanner<T> input, Comparator<? super T> comparator){
		super(input);
		this.comparator = comparator;
	}

	@Override
	public boolean advanceInternal(){
		if(input.advance()){
			current = Objects.requireNonNull(input.current());
			return true;
		}
		current = null;
		return false;
	}

	@Override
	public int compareTo(Scanner<T> that){
		return comparator.compare(current(), that.current());
	}

}

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

import java.util.Objects;

public class SamplingScanner<T> extends BaseLinkedScanner<T,T>{

	private final long sampleSize;
	private final boolean includeLast;

	private long counter;
	private T previousInput;
	private boolean finished;

	public SamplingScanner(Scanner<T> input, long sampleSize, boolean includeLast){
		super(input);
		this.sampleSize = sampleSize;
		this.includeLast = includeLast;
		this.counter = 0;
	}

	@Override
	public boolean advanceInternal(){
		if(finished){
			return false;
		}
		while(input.advance()){
			previousInput = input.current();
			++counter;
			long modulo = counter % sampleSize;
			if(modulo == 0){
				current = input.current();
				return true;
			}
		}
		if(includeLast && counter > 0 && !Objects.equals(previousInput, current)){
			current = previousInput;
			finished = true;
			return true;
		}
		return false;
	}

}

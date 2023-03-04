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

import java.util.function.Consumer;

public class CountingPeriodicScanner<T> extends BaseLinkedScanner<T,T>{

	private final long period;
	private final Consumer<? super T> consumer;
	private long count;
	private long nextActionableCount;

	public CountingPeriodicScanner(Scanner<T> input, long period, Consumer<? super T> consumer){
		super(input);
		this.period = period;
		this.consumer = consumer;
		count = 0;
		nextActionableCount = period;
	}

	@Override
	public boolean advanceInternal(){
		if(input.advance()){
			current = input.current();
			++count;
			if(count == nextActionableCount){
				consumer.accept(current);
				nextActionableCount += period;
			}
			return true;
		}
		return false;
	}

}
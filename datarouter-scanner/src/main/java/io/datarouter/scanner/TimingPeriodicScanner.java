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

import java.time.Duration;
import java.util.function.Consumer;

public class TimingPeriodicScanner<T> extends BaseLinkedScanner<T,T>{

	private final long periodNs;
	private final Consumer<? super T> consumer;
	private long nextActionableTimeNs;

	public TimingPeriodicScanner(Scanner<T> input, Duration period, Consumer<? super T> consumer){
		super(input);
		this.periodNs = period.toNanos();
		this.consumer = consumer;
		nextActionableTimeNs = System.nanoTime() + periodNs;
	}

	@Override
	public boolean advanceInternal(){
		if(input.advance()){
			current = input.current();
			long nowNs = System.nanoTime();
			if(nowNs >= nextActionableTimeNs){
				consumer.accept(current);
				nextActionableTimeNs += periodNs;
			}
			return true;
		}
		return false;
	}

}
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
import java.time.Instant;

public class StaggeringScanner<T> extends BaseLinkedScanner<T,T>{

	private final long totalItems;
	private final Duration duration;

	private final Instant startTime;
	private long numCompleted;

	public StaggeringScanner(Scanner<T> input, long totalItems, Duration duration){
		super(input);
		this.totalItems = totalItems;
		this.duration = duration;
		startTime = Instant.now();
		numCompleted = 0;
	}

	@Override
	public boolean advanceInternal(){
		while(input.advance()){
			if(numCompleted > 0){
				delayNextItem();
			}
			current = input.current();
			++numCompleted;
			return true;
		}
		return false;
	}

	private void delayNextItem(){
		double fractionComplete = (double)numCompleted / (double)totalItems;
		long nextOffsetMs = (long)(fractionComplete * duration.toMillis());
		long nextStartMs = startTime.toEpochMilli() + nextOffsetMs;
		long sleepMs = Math.max(0, nextStartMs - System.currentTimeMillis());
		try{
			Thread.sleep(sleepMs);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}

}

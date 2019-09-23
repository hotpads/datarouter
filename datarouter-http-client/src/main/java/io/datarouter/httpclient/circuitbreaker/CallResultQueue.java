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
package io.datarouter.httpclient.circuitbreaker;

import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;

public class CallResultQueue{

	private final boolean[] results;
	private Clock clock;

	private int currentIndex;
	public Optional<Long> lastFailureEpochMillis = Optional.empty();

	public CallResultQueue(Clock clock, int queueSize){
		this.results = new boolean[queueSize];
		this.clock = clock;
		reset();
	}

	public synchronized void insertResult(boolean result){
		results[currentIndex] = result;
		currentIndex = (currentIndex + 1) % results.length;
		if(!result){
			lastFailureEpochMillis = Optional.of(clock.millis());
		}
	}

	public float getFailurePercentage(){
		int total = 0;
		int failures = 0;
		int startingIndex = getPreviousPosition(currentIndex);
		int position = startingIndex;
		do{
			if(!results[position]){
				failures++;
			}
			total++;
			position = getPreviousPosition(position);
		}while(position != startingIndex);
		return total == 0 ? 0 : 100F * failures / total;
	}

	public void reset(){
		Arrays.fill(this.results, true);
		lastFailureEpochMillis = Optional.empty();
	}

	private int getPreviousPosition(int position){
		return position <= 0 ? results.length - 1 : position - 1;
	}

	public boolean[] getResults(){
		return results;
	}

	protected void setClock(Clock clock){
		this.clock = clock;
	}

}

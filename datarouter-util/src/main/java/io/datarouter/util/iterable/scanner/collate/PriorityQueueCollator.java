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
package io.datarouter.util.iterable.scanner.collate;

import java.util.PriorityQueue;

import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.iterable.scanner.sorted.BaseSortedScanner;
import io.datarouter.util.iterable.scanner.sorted.SortedScanner;

public class PriorityQueueCollator<T extends Comparable<? super T>>
extends BaseSortedScanner<T>{

	//all scanners in pq should be active, meaning scanner.getCurrent() != null
	private final PriorityQueue<SortedScanner<T>> pq = new PriorityQueue<>();
	private final Iterable<? extends SortedScanner<T>> scanners;
	private final Long limit;
	private long offset;
	private T current;
	private SortedScanner<T> nextScanner;

	public PriorityQueueCollator(Iterable<? extends SortedScanner<T>> scanners, Long limit){
		this.limit = limit;
		this.offset = 0;
		this.scanners = scanners;
		for(SortedScanner<T> scanner : IterableTool.nullSafe(scanners)){
			if(scanner.advance()){
				pq.add(scanner);
			}
		}
		this.nextScanner = pq.poll();
	}

	public PriorityQueueCollator(Iterable<? extends SortedScanner<T>> scanners){
		this(scanners, null);
	}

	@Override
	public T getCurrent(){
		return current;
	}

	@Override
	public boolean advance(){
		current = null;
		if(limit != null && offset >= limit){
			scanners.forEach(SortedScanner::cleanup);
			return false;
		}
		if(nextScanner == null){
			scanners.forEach(SortedScanner::cleanup);
			return false;
		}
		current = nextScanner.getCurrent();
		updateNextScanner();
		++offset;
		return true;
	}

	private void updateNextScanner(){
		if(!nextScanner.advance()){// this scanner finished. grab one from the pq
			nextScanner = pq.poll();
			return;
		}
		if(pq.isEmpty()){
			return;//we're on the last scanner, so nothing else to check
		}
		if(nextScanner.compareTo(pq.peek()) < 0){//optimization to skip the pq
			return;//currentScanner is still the best choice
		}
		//optimization failed.  get a new scanner from the PriorityQueue
		SortedScanner<T> tempScanner = nextScanner;
		nextScanner = pq.poll();//should not be null
		pq.add(tempScanner);
	}

}

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

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollatingScanner<T> extends BaseScanner<T>{
	private static final Logger logger = LoggerFactory.getLogger(CollatingScanner.class);

	private final PriorityQueue<ComparableScanner<T>> priorityQueue;

	private boolean closed;

	public CollatingScanner(List<Scanner<T>> inputs, Comparator<? super T> comparator){
		this.priorityQueue = new PriorityQueue<>();
		this.closed = false;
		Scanner.of(inputs)
				.map(input -> new ComparableScanner<>(input, comparator))
				.include(Scanner::advance)
				.forEach(priorityQueue::add);
	}

	@Override
	public boolean advance(){
		if(closed){
			return false;
		}
		ComparableScanner<T> firstScanner = priorityQueue.poll();
		if(firstScanner == null){
			current = null;
			return false;
		}
		current = firstScanner.current();
		if(firstScanner.advance()){
			priorityQueue.add(firstScanner);
		}else{
			firstScanner.close();
		}
		return true;
	}

	@Override
	public void close(){
		if(closed){
			return;
		}
		for(Scanner<T> input : priorityQueue){
			try{
				input.close();
			}catch(Exception e){
				logger.warn("scanner exception on input.close", e);
			}
		}
		closed = true;
	}

}

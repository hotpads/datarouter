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
package io.datarouter.util.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.util.concurrent.BlockingQueueTool;

public class MemoryBuffer<T> implements Buffer{

	private final String name;
	private final BlockingQueue<T> queue;

	public MemoryBuffer(String name, int maxSize){
		this.name = name;
		this.queue = new ArrayBlockingQueue<>(maxSize);
	}

	@Override
	public String getName(){
		return name;
	}

	/**
	 * Caller will block until space becomes available in the queue.
	 */
	public void put(T obj){
		BlockingQueueTool.put(queue, obj);
	}

	/**
	 * Item will be discarded if the queue is full.
	 */
	public boolean offer(T obj){
		boolean accepted = queue.offer(obj);
		if(!accepted){
			Metrics.count("MemoryBuffer rejection " + name);
		}
		return accepted;
	}

	public boolean offerMulti(Collection<T> objects){
		for(T obj : objects){
			if(!offer(obj)){
				return false;
			}
		}
		return true;
	}

	public Optional<T> poll(){
		return Optional.ofNullable(queue.poll());
	}

	public List<T> pollMultiWithLimit(int limit){
		List<T> result = new ArrayList<>();
		while(result.size() < limit){
			T obj = queue.poll();
			if(obj == null){
				break;
			}
			result.add(obj);
		}
		return result;
	}

}

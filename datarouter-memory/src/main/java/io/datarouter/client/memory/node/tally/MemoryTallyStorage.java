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
package io.datarouter.client.memory.node.tally;

import java.util.HashMap;
import java.util.Map;

import io.datarouter.client.memory.util.CloseableReentrantReadWriteLock;

/**
 * Internal tally storage with locking and TTLs
 *
 * TODO vacuum expired entries
 */
public class MemoryTallyStorage{

	private final Map<String,MemoryTally> tallyById;
	private final CloseableReentrantReadWriteLock lock;

	public MemoryTallyStorage(){
		tallyById = new HashMap<>();
		lock = new CloseableReentrantReadWriteLock();
	}

	/*-------------- write ---------------*/

	public long addAndGet(String id, long delta, Long ttlMs){
		try(var _ = lock.lockForWriting()){
			MemoryTally memoryTally = tallyById.get(id);
			if(memoryTally == null || memoryTally.isExpired()){
				memoryTally = new MemoryTally(ttlMs);
				tallyById.put(id, memoryTally);
			}
			return memoryTally.addAndGet(delta);
		}
	}

	public void delete(String id){
		try(var _ = lock.lockForWriting()){
			tallyById.remove(id);
		}
	}

	/*-------------- read ----------------*/

	public MemoryTally get(String id){
		try(var _ = lock.lockForReading()){
			return tallyById.get(id);
		}
	}

}

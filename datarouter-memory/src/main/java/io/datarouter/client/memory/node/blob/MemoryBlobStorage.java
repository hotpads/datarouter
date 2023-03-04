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
package io.datarouter.client.memory.node.blob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import io.datarouter.client.memory.util.CloseableReentrantReadWriteLock;
import io.datarouter.client.memory.util.RangeMap;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;

public class MemoryBlobStorage{

	private final NavigableMap<byte[],MemoryBlob> navigableMap;
	private final RangeMap<byte[],MemoryBlob> rangeMap;
	private final CloseableReentrantReadWriteLock lock;

	public MemoryBlobStorage(){
		navigableMap = new TreeMap<>(Arrays::compareUnsigned);
		rangeMap = new RangeMap<>(navigableMap);
		lock = new CloseableReentrantReadWriteLock();
	}

	public NavigableMap<byte[],MemoryBlob> getNavigableMap(){
		return navigableMap;
	}

	public RangeMap<byte[],MemoryBlob> getRangeMap(){
		return rangeMap;
	}

	/*-------------- write ---------------*/

	public void write(byte[] key, byte[] value, Long ttlMs){
		try(var $ = lock.lockForWriting()){
			navigableMap.put(key, new MemoryBlob(key, value, ttlMs));
		}
	}

	public void delete(byte[] key){
		try(var $ = lock.lockForWriting()){
			navigableMap.remove(key);
		}
	}

	public void deleteAll(){
		try(var $ = lock.lockForWriting()){
			navigableMap.clear();
		}
	}

	/*-------------- read ----------------*/

	public Optional<MemoryBlob> find(byte[] key){
		MemoryBlob result;
		try(var $ = lock.lockForReading()){
			result = navigableMap.get(key);
		}
		return Optional.ofNullable(result)
				.filter(MemoryBlob::notExpired);
	}

	public Scanner<MemoryBlob> scan(Range<byte[]> range){
		List<MemoryBlob> result;
		try(var $ = lock.lockForReading()){
			result = new ArrayList<>(rangeMap.subMap(range).values());
		}
		return Scanner.of(result)
				.exclude(MemoryBlob::isExpired);
	}

}

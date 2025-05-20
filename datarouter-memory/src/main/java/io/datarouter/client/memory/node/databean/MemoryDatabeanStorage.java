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
package io.datarouter.client.memory.node.databean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import io.datarouter.client.memory.util.CloseableReentrantReadWriteLock;
import io.datarouter.client.memory.util.RangeMap;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;

/**
 * Internal databean storage with locking and TTLs
 *
 * TODO vacuum expired entries
 * TODO store key as byte[], but need Range accepting non-Comparable
 */
public class MemoryDatabeanStorage{

	private final NavigableMap<byte[],MemoryDatabean> navigableMap;
	private final RangeMap<byte[],MemoryDatabean> rangeMap;
	private final CloseableReentrantReadWriteLock lock;

	public MemoryDatabeanStorage(){
		navigableMap = new TreeMap<>(Arrays::compareUnsigned);
		rangeMap = new RangeMap<>(navigableMap);
		lock = new CloseableReentrantReadWriteLock();
	}

	public NavigableMap<byte[],MemoryDatabean> getNavigableMap(){
		return navigableMap;
	}

	public RangeMap<byte[],MemoryDatabean> getRangeMap(){
		return rangeMap;
	}

	/*-------------- write ---------------*/

	public void put(List<MemoryDatabeanAndIndexEntries> rows, Long ttlMs){
		try(var _ = lock.lockForWriting()){
			rows.forEach(row -> navigableMap.put(
					row.key,
					new MemoryDatabean(row.key, row, ttlMs)));
		}
	}

	public void delete(List<byte[]> keys){
		try(var _ = lock.lockForWriting()){
			keys.forEach(navigableMap::remove);
		}
	}

	public void deleteAll(){
		try(var _ = lock.lockForWriting()){
			navigableMap.clear();
		}
	}

	/*-------------- read ----------------*/

	public Optional<MemoryDatabean> find(byte[] key){
		MemoryDatabean result;
		try(var _ = lock.lockForReading()){
			result = navigableMap.get(key);
		}
		return Optional.ofNullable(result)
				.filter(MemoryDatabean::notExpired);
	}

	public Scanner<MemoryDatabean> scanMulti(List<byte[]> keys){
		List<MemoryDatabean> result;
		try(var _ = lock.lockForReading()){
			result = Scanner.of(keys)
					.map(navigableMap::get)
					.list();
		}
		return Scanner.of(result)
				.exclude(Objects::isNull)
				.exclude(MemoryDatabean::isExpired);
	}

	public Scanner<MemoryDatabean> scan(Range<byte[]> range){
		List<MemoryDatabean> result;
		try(var _ = lock.lockForReading()){
			result = new ArrayList<>(rangeMap.subMap(range).values());
		}
		return Scanner.of(result)
				.exclude(MemoryDatabean::isExpired);
	}

}

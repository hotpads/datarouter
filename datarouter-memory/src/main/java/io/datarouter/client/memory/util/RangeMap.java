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
package io.datarouter.client.memory.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;

import io.datarouter.util.tuple.Range;

/**
 * Wrapper around NavigableMap to support Range queries
 */
public class RangeMap<K,V>{

	private final NavigableMap<K,V> map;

	public RangeMap(NavigableMap<K,V> map){
		this.map = map;
	}

	public List<V> listValues(Range<K> range){
		Collection<V> values = subMap(range).values();
		return new ArrayList<>(values);//copy to detach from underlying map
	}

	public NavigableMap<K,V> subMap(Range<K> range){
		if(range.hasStart() && range.hasEnd()){
			return map.subMap(
					range.getStart(),
					range.getStartInclusive(),
					range.getEnd(),
					range.getEndInclusive());
		}
		if(range.hasStart()){
			return map.tailMap(
					range.getStart(),
					range.getStartInclusive());
		}
		if(range.hasEnd()){
			return map.headMap(
					range.getEnd(),
					range.getEndInclusive());
		}
		return map;
	}

}

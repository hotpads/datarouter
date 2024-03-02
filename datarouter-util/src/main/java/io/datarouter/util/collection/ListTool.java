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
package io.datarouter.util.collection;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.datarouter.util.ComparableTool;

public class ListTool{

	/**
	 * @deprecated  Use List.getLast() or ListTool.findLast(list).orElse(null)
	 */
	@Deprecated
	public static <T> T getLastOrNull(List<T> list){
		return list.isEmpty() ? null : list.getLast();
	}

	public static <T> Optional<T> findLast(List<T> list){
		return list.isEmpty() ? Optional.empty() : Optional.of(list.getLast());
	}

	public static <T extends Comparable<T>> int compare(List<T> as, List<T> bs){
		if(as == null){
			return bs == null ? 0 : -1;
		}
		if(bs == null){
			return 1;
		}
		Iterator<T> bi = bs.iterator();
		for(T a : as){
			if(!bi.hasNext()){
				return 1;
			} // as are longer than bs
			int comp = ComparableTool.nullFirstCompareTo(a, bi.next());
			if(comp != 0){
				return comp;
			}
		}
		return bi.hasNext() ? -1 : 0; // bs are longer than as
	}

}

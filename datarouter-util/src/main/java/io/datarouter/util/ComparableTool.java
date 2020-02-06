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
package io.datarouter.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ComparableTool{

	public static <T extends Comparable<? super T>> boolean lt(T object1, T object2){
		int diff = nullFirstCompareTo(object1, object2);
		return diff < 0;
	}

	/**
	 * is a &gt; b
	 */
	public static <T extends Comparable<? super T>> boolean gt(T object1, T object2){
		int diff = nullFirstCompareTo(object1, object2);
		return diff > 0;
	}

	//treat start=null as -Infinity, end=null as Infinity
	public static <T extends Comparable<? super T>> boolean between(T min, boolean minInclusive, T value, T max,
			boolean maxInclusive){
		int minDiff = nullFirstCompareTo(value, min);
		int maxDiff = nullLastCompareTo(value, max);
		if(minInclusive && minDiff == 0){
			return true;
		}
		if(maxInclusive && maxDiff == 0){
			return true;
		}
		return minDiff > 0 && maxDiff < 0;
	}

	public static <T extends Comparable<? super T>> int nullFirstCompareTo(T object1, T object2){
		if(object1 == null && object2 == null){
			return 0;
		}else if(object1 == null){
			return -1;
		}else if(object2 == null){
			return 1;
		}else{
			return object1.compareTo(object2);
		}
	}

	public static <T extends Comparable<? super T>> int nullLastCompareTo(T object1, T object2){
		if(object1 == null && object2 == null){
			return 0;
		}else if(object1 == null){
			return 1;
		}else if(object2 == null){
			return -1;
		}else{
			return object1.compareTo(object2);
		}
	}

	/**
	 * null is greater than non-null
	 */
	@SafeVarargs
	public static <T extends Comparable<T>> T min(T... elements){
		return min(Arrays.asList(elements));
	}

	/**
	 * null is greater than non-null
	 */
	public static <T extends Comparable<T>> T min(Collection<T> elements){
		return Collections.min(elements, Comparator.nullsLast(Comparator.naturalOrder()));
	}

	/**
	 * null is less than non-null
	 */
	@SafeVarargs
	public static <T extends Comparable<T>> T max(T... elements){
		return max(Arrays.asList(elements));
	}

	/**
	 * null is less than non-null
	 */
	public static <T extends Comparable<T>> T max(Collection<T> elements){
		return Collections.max(elements, Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	public static <T extends Comparable<? super T>> boolean isSorted(Iterable<? extends T> ins){
		if(ins == null){
			return true;
		}//null is considered sorted
		Iterator<? extends T> iter = ins.iterator();
		if(!iter.hasNext()){
			return true;
		}//0 elements is sorted
		T previous = iter.next();
		if(!iter.hasNext()){
			return true;
		}//1 element is sorted
		T current = null;
		while(iter.hasNext()){
			current = iter.next();
			if(previous.compareTo(current) > 0){
				return false;
			}
			previous = current;
		}
		return true;
	}

}

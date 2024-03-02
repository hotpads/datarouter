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
package io.datarouter.bytes.blockfile.index;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BlockfilePointSearchTool{

	public enum BlockfileBinarySearchType{
		ANY,
		FIRST,
		LAST;
	}

	/**
	 * @param <C>  sorted items accessible by integer index
	 * @param <I>  a single item
	 */
	public static <C,I> int binarySearch(
			int size,
			Function<Integer,I> extractFn,
			Function<I,Integer> compareFn,
			BlockfileBinarySearchType searchType){
		int low = 0;
		int high = size - 1;
		int mid = 0;
		int comp = 0;
		Integer resultIndex = null;
		while(low <= high){
			mid = low + (high - low) / 2;
			I item = extractFn.apply(mid);
			comp = compareFn.apply(item);
			if(comp < 0){
				low = mid + 1;
			}else if(comp == 0){
				resultIndex = mid;
				if(searchType == BlockfileBinarySearchType.ANY){
					return mid;
				}else if(searchType == BlockfileBinarySearchType.FIRST){
					high = mid - 1;
				}else if(searchType == BlockfileBinarySearchType.LAST){
					low = mid + 1;
				}
			}else if(comp > 0){
				high = mid - 1;
			}
		}
		return resultIndex != null ? resultIndex : -mid - 1;
	}

	/*---------- find -----------*/

	public static <C,I> Optional<I> find(
			int size,
			Function<Integer,I> extractFn,
			Function<I,Integer> compareFn,
			BlockfileBinarySearchType searchType){
		int index = binarySearch(size, extractFn, compareFn, searchType);
		return index < 0 ? Optional.empty() : Optional.of(extractFn.apply(index));
	}

	public static <C,I,K> Optional<I> find(
			C items,
			int size,
			BiFunction<C,Integer,I> extractBiFn,
			BiFunction<I,K,Integer> compareBiFn,
			K searchKey,
			BlockfileBinarySearchType searchType){
		Function<Integer,I> extractFn = index -> extractBiFn.apply(items, index);
		Function<I,Integer> compareFn = item -> compareBiFn.apply(item, searchKey);
		return find(size, extractFn, compareFn, searchType);
	}

	public static <I,K> Optional<I> findInList(
			List<I> items,
			BiFunction<I,K,Integer> compareBiFn,
			K searchKey,
			BlockfileBinarySearchType searchType){
		Function<Integer,I> extractFn = index -> items.get(index);
		Function<I,Integer> compareFn = item -> compareBiFn.apply(item, searchKey);
		return find(items.size(), extractFn, compareFn, searchType);
	}

	/*--------- find any ---------*/

	/**
	 * @param <C>  sorted items accessible by integer index
	 * @param <I>  a single item
	 */
	public static <C,I> Optional<I> findAny(
			int size,
			Function<Integer,I> extractFn,
			Function<I,Integer> compareFn){
		return find(size, extractFn, compareFn, BlockfileBinarySearchType.ANY);
	}

	public static <C,I,K> Optional<I> findAny(
			C items,
			int size,
			BiFunction<C,Integer,I> extractBiFn,
			BiFunction<I,K,Integer> compareBiFn,
			K searchKey){
		return find(items, size, extractBiFn, compareBiFn, searchKey, BlockfileBinarySearchType.ANY);
	}

	public static <I,K> Optional<I> findAnyInList(
			List<I> items,
			BiFunction<I,K,Integer> compareBiFn,
			K searchKey){
		return findInList(items, compareBiFn, searchKey, BlockfileBinarySearchType.ANY);
	}

	/*--------- find first ---------*/

	/**
	 * @param <C>  sorted items accessible by integer index
	 * @param <I>  a single item
	 */
	public static <C,I> Optional<I> findFirst(
			int size,
			Function<Integer,I> extractFn,
			Function<I,Integer> compareFn){
		return find(size, extractFn, compareFn, BlockfileBinarySearchType.FIRST);
	}

	public static <C,I,K> Optional<I> findFirst(
			C items,
			int size,
			BiFunction<C,Integer,I> extractBiFn,
			BiFunction<I,K,Integer> compareBiFn,
			K searchKey){
		return find(items, size, extractBiFn, compareBiFn, searchKey, BlockfileBinarySearchType.FIRST);
	}

	public static <I,K> Optional<I> findFirstInList(
			List<I> items,
			BiFunction<I,K,Integer> compareBiFn,
			K searchKey){
		return findInList(items, compareBiFn, searchKey, BlockfileBinarySearchType.FIRST);
	}

	/*--------- find last ---------*/

	/**
	 * @param <C>  sorted items accessible by integer index
	 * @param <I>  a single item
	 */
	public static <C,I> Optional<I> findLast(
			int size,
			Function<Integer,I> extractFn,
			Function<I,Integer> compareFn){
		return find(size, extractFn, compareFn, BlockfileBinarySearchType.LAST);
	}

	public static <C,I,K> Optional<I> findLast(
			C items,
			int size,
			BiFunction<C,Integer,I> extractBiFn,
			BiFunction<I,K,Integer> compareBiFn,
			K searchKey){
		return find(items, size, extractBiFn, compareBiFn, searchKey, BlockfileBinarySearchType.LAST);
	}

	public static <I,K> Optional<I> findLastInList(
			List<I> items,
			BiFunction<I,K,Integer> compareBiFn,
			K searchKey){
		return findInList(items, compareBiFn, searchKey, BlockfileBinarySearchType.LAST);
	}
}

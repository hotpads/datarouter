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

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockfileRangeSearchTool{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileRangeSearchTool.class);

	public static <C,I> int startIndex(
			int size,
			Function<Integer,I> extractFn,
			Function<I,Integer> compareFn){
		int low = 0;
		int high = size - 1;
		int mid = 0;
		int comp = 0;
		while(low <= high){
			mid = low + (high - low) / 2;
			comp = compareFn.apply(extractFn.apply(mid));
			if(comp < 0){
				low = mid + 1;
			}else{
				high = mid - 1;
			}
		}
		comp = compareFn.apply(extractFn.apply(mid));
		if(comp < 0){
			return mid + 1;
		}else if(comp == 0){
			return mid;
		}else{
			return mid == 0 ? -1 : mid;
		}
	}

	public static <C,I> int endIndex(
			int size,
			Function<Integer,I> extractFn,
			Function<I,Integer> compareFn){
		int low = 0;
		int high = size - 1;
		int mid = 0;
		int comp = 0;
		while(low <= high){
			mid = low + (high - low) / 2;
			comp = compareFn.apply(extractFn.apply(mid));
			if(comp <= 0){
				low = mid + 1;
			}else{
				high = mid - 1;
			}
		}
		comp = compareFn.apply(extractFn.apply(mid));
		if(comp <= 0){
			return mid;
		}else{
			return mid - 1;
		}
	}

}

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
package io.datarouter.util.iterable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.ComparableTool;

public class IterableTool{

	public static <T> Iterable<T> nullSafe(Iterable<T> in){
		if(in == null){
			return new ArrayList<>();
		}
		return in;
	}

	public static <A,T> List<T> nullSafeMap(Iterable<A> iterable, Function<A,T> mapper){
		if(iterable == null){
			return new ArrayList<>();
		}
		return Scanner.of(iterable).map(mapper).list();
	}

	public static <T extends Comparable<? super T>> boolean isSorted(Iterable<T> items){
		T last = null;
		for(T item : items){
			if(ComparableTool.nullFirstCompareTo(last, item) > 0){
				return false;
			}
			last = item;
		}
		return true;
	}

}

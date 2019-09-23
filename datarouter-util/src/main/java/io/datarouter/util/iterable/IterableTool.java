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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.datarouter.util.StreamTool;

public class IterableTool{

	public static <T> Iterable<T> nullSafe(Iterable<T> in){
		if(in == null){
			return new ArrayList<>();
		}
		return in;
	}

	public static <T> void forEach(Iterable<T> iterable, Consumer<? super T> action){
		StreamTool.stream(iterable).forEach(action);
	}

	public static <A,T> List<T> map(Iterable<A> iterable, Function<A,T> mapper){
		return StreamTool.map(StreamTool.stream(iterable), mapper);
	}

	public static <A,T> Set<T> mapToSet(Iterable<A> iterable, Function<A,T> mapper){
		return StreamTool.mapToSet(StreamTool.stream(iterable), mapper);
	}

	public static <A> List<A> filter(Iterable<A> iterable, Predicate<A> filter){
		return StreamTool.stream(iterable)
				.filter(filter)
				.collect(Collectors.toList());
	}

	public static <A> List<A> include(Iterable<A> iterable, Predicate<A> filter){
		return StreamTool.stream(iterable)
				.filter(filter)
				.collect(Collectors.toList());
	}

	public static <A> List<A> exclude(Iterable<A> iterable, Predicate<A> filter){
		return StreamTool.stream(iterable)
				.filter(filter.negate())
				.collect(Collectors.toList());
	}

	public static <T> T next(Iterator<T> iterator){
		if(iterator == null){
			return null;
		}
		return iterator.hasNext() ? iterator.next() : null;
	}

	public static <T> T first(Iterable<T> iterable){
		if(iterable == null){
			return null;
		}
		return next(iterable.iterator());
	}

	public static <T> Long count(Iterable<T> iterable){
		if(iterable == null){
			return 0L;
		}
		if(iterable instanceof Collection){
			return (long)((Collection<T>)iterable).size();
		}
		long count = 0;
		Iterator<T> iterator = iterable.iterator();
		while(iterator.hasNext()){
			++count;
			iterator.next();
		}
		return count;
	}

	public static <T> ArrayList<T> createArrayListFromIterable(Iterable<T> ins){
		return createArrayListFromIterable(ins, Integer.MAX_VALUE);
	}

	public static <T> ArrayList<T> createArrayListFromIterable(Iterable<T> ins, int limit){
		ArrayList<T> outs = new ArrayList<>();
		int numAdded = 0;
		for(T in : nullSafe(ins)){
			outs.add(in);
			++numAdded;
			if(numAdded >= limit){
				break;
			}
		}
		return outs;
	}

	public static <T> Iterable<T> skip(Iterable<T> ins, Long nullableSkip){
		long skip = Optional.ofNullable(nullableSkip)
				.orElse(0L)
				.longValue();
		return () -> {
			Iterator<T> iterator = ins.iterator();
			for(int i = 0; i < skip && iterator.hasNext(); i++){
				iterator.next();
			}
			return iterator;
		};
	}

	public static <E> List<E> asList(Iterable<E> iterable){
		if(iterable instanceof List){
			return (List<E>)iterable;
		}
		ArrayList<E> list = new ArrayList<>();
		if(iterable != null){
			for(E e : iterable){
				list.add(e);
			}
		}
		return list;
	}

}

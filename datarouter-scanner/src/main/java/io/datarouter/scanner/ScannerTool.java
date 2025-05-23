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
package io.datarouter.scanner;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ScannerTool{

	public static <T> boolean allMatch(Scanner<T> scanner, Predicate<? super T> predicate){
		try(Scanner<T> _ = scanner){
			while(scanner.advance()){
				if(!predicate.test(scanner.current())){
					return false;
				}
			}
			return true;
		}
	}

	public static <T> boolean anyMatch(Scanner<T> scanner, Predicate<? super T> predicate){
		try(Scanner<T> _ = scanner){
			while(scanner.advance()){
				if(predicate.test(scanner.current())){
					return true;
				}
			}
			return false;
		}
	}

	public static <T,C extends Collection<T>> C collect(Scanner<T> scanner, Supplier<C> collectionSupplier){
		try(Scanner<T> _ = scanner){
			C collection = collectionSupplier.get();
			while(scanner.advance()){
				collection.add(scanner.current());
			}
			return collection;
		}
	}

	public static <T> long count(Scanner<T> scanner){
		try(Scanner<T> _ = scanner){
			long count = 0;
			while(scanner.advance()){
				++count;
			}
			return count;
		}
	}

	public static <T> int countInt(Scanner<T> scanner){
		try(Scanner<T> _ = scanner){
			long count = 0;
			while(scanner.advance()){
				++count;
				if(count > Integer.MAX_VALUE){
					throw new RuntimeException("Exceeded max int value");
				}
			}
			return Math.toIntExact(count);
		}
	}

	public static <T> Optional<T> findFirst(Scanner<T> scanner){
		try(Scanner<T> _ = scanner){
			if(scanner.advance()){
				return Optional.of(scanner.current());
			}
			return Optional.empty();
		}
	}

	public static <T> Optional<T> findLast(Scanner<T> scanner){
		try(Scanner<T> _ = scanner){
			T last = null;
			boolean foundAny = false;
			while(scanner.advance()){
				last = scanner.current();
				foundAny = true;
			}
			return foundAny ? Optional.of(last) : Optional.empty();
		}
	}

	public static <T> Scanner<T> flush(Scanner<T> scanner, Consumer<List<T>> consumer){
		List<T> list = Collections.unmodifiableList(list(scanner));
		consumer.accept(list);
		return Scanner.of(list);
	}

	public static <T> void forEach(Scanner<T> scanner, Consumer<? super T> action){
		try(Scanner<T> _ = scanner){
			while(scanner.advance()){
				action.accept(scanner.current());
			}
		}
	}

	public static <T> boolean hasAny(Scanner<T> scanner){
		try(Scanner<T> _ = scanner){
			return scanner.advance();
		}
	}

	public static <T> boolean isEmpty(Scanner<T> scanner){
		try(Scanner<T> _ = scanner){
			return !scanner.advance();
		}
	}

	public static <T> List<T> list(Scanner<T> scanner){
		try(Scanner<T> _ = scanner){
			List<T> list = new ArrayList<>();
			while(scanner.advance()){
				list.add(scanner.current());
			}
			return Collections.unmodifiableList(list);
		}
	}

	public static <T> Optional<T> max(Scanner<T> scanner, Comparator<? super T> comparator){
		try(Scanner<T> _ = scanner){
			T max = null;
			while(scanner.advance()){
				T current = scanner.current();
				if(max == null || comparator.compare(current, max) > 0){
					max = current;
				}
			}
			return Optional.ofNullable(max);
		}
	}

	public static <T> Optional<T> min(Scanner<T> scanner, Comparator<? super T> comparator){
		try(Scanner<T> _ = scanner){
			T min = null;
			while(scanner.advance()){
				T current = scanner.current();
				if(min == null || comparator.compare(current, min) < 0){
					min = current;
				}
			}
			return Optional.ofNullable(min);
		}
	}

	public static <T> Scanner<T> maxNDesc(Scanner<T> scanner, Comparator<? super T> comparator, int num){
		List<T> maxNAsc = maxNAsc(scanner, comparator, num);
		return ReverseListScanner.of(maxNAsc);
	}

	public static <T> Scanner<T> minNAsc(Scanner<T> scanner, Comparator<? super T> comparator, int num){
		List<T> minNDesc = maxNAsc(scanner, comparator.reversed(), num);
		return ReverseListScanner.of(minNDesc);
	}

	private static <T> List<T> maxNAsc(Scanner<T> scanner, Comparator<? super T> comparator, int num){
		try(Scanner<T> _ = scanner){
			if(num == 0){
				return List.of();
			}
			if(num == 1){
				return max(scanner, comparator)
						.map(Collections::singletonList)
						.orElseGet(Collections::emptyList);
			}
			PriorityQueue<T> heap = new PriorityQueue<>(comparator);
			while(scanner.advance()){
				T current = scanner.current();
				Objects.requireNonNull(current, "PriorityQueue implementation doesn't support nulls");
				heap.add(current);
				if(heap.size() > num){
					heap.poll();
				}
			}
			int size = heap.size();
			List<T> ordered = new ArrayList<>(size);
			for(int i = 0; i < size; ++i){
				ordered.add(heap.poll());
			}
			return ordered;
		}
	}

	public static <T> Stream<T> nativeStream(Scanner<T> scanner){
		Spliterator<T> spliterator = spliterator(scanner);
		Stream<T> stream = StreamSupport.stream(spliterator, false);
		stream.onClose(scanner::close);
		return stream;
	}

	public static <T> boolean noneMatch(Scanner<T> scanner, Predicate<? super T> predicate){
		try(Scanner<T> _ = scanner){
			while(scanner.advance()){
				if(predicate.test(scanner.current())){
					return false;
				}
			}
			return true;
		}
	}

	public static <T> Scanner<T> peekFirst(Scanner<T> scanner, Consumer<? super T> action){
		List<T> item = scanner.take(1);
		if(item.isEmpty()){
			return new EmptyScanner<>();
		}
		action.accept(item.getFirst());
		return ObjectScanner.of(item.getFirst()).append(scanner);
	}

	public static <T> Optional<T> reduce(Scanner<T> scanner, BinaryOperator<T> reducer){
		try(Scanner<T> _ = scanner){
			T result = null;
			boolean foundAny = false;
			while(scanner.advance()){
				if(!foundAny){
					result = scanner.current();
				}else{
					result = reducer.apply(result, scanner.current());
				}
				foundAny = true;
			}
			return foundAny ? Optional.of(result) : Optional.empty();
		}
	}

	public static <T> T reduce(Scanner<T> scanner, T seed, BinaryOperator<T> reducer){
		try(Scanner<T> _ = scanner){
			T result = seed;
			while(scanner.advance()){
				result = reducer.apply(result, scanner.current());
			}
			return result;
		}
	}

	public static Scanner<Integer> scanBits(BitSet bitSet){
		Stream<Integer> stream = bitSet.stream()
				.mapToObj(Integer::valueOf);
		return Scanner.of(stream);
	}

	public static <T> Scanner<T> skip(Scanner<T> scanner, long numToSkip){
		long numSkipped = 0;
		while(numSkipped < numToSkip){
			if(!scanner.advance()){
				scanner.close();
				return scanner;
			}
			++numSkipped;
		}
		return scanner;
	}

	public static <T> Spliterator<T> spliterator(Scanner<T> scanner){
		return Spliterators.spliteratorUnknownSize(scanner.iterator(), 0);
	}

	public static <T> ScannerNextItem<T> next(Scanner<T> scanner){
		if(!scanner.advance()){
			scanner.close();
			return new ScannerNextItem<>(false, null);
		}
		return new ScannerNextItem<>(true, scanner.current());
	}

	public static <T> List<T> take(Scanner<T> scanner, int numToTake){
		List<T> items = new ArrayList<>(numToTake);
		while(items.size() < numToTake){
			if(!scanner.advance()){
				scanner.close();
				return items;
			}
			items.add(scanner.current());
		}
		return items;
	}

	public static Object[] toArray(Scanner<?> scanner){
		try(Scanner<?> _ = scanner){
			return list(scanner).toArray();
		}
	}

}

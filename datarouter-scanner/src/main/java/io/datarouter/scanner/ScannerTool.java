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
package io.datarouter.scanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ScannerTool{

	public static <T> boolean allMatch(Scanner<T> scanner, Predicate<? super T> predicate){
		try(Scanner<T> ref = scanner){
			while(scanner.advance()){
				if(!predicate.test(scanner.current())){
					return false;
				}
			}
			return true;
		}
	}

	public static <T> boolean anyMatch(Scanner<T> scanner, Predicate<? super T> predicate){
		try(Scanner<T> ref = scanner){
			while(scanner.advance()){
				if(predicate.test(scanner.current())){
					return true;
				}
			}
			return false;
		}
	}

	public static <T> long count(Scanner<T> scanner){
		try(Scanner<T> ref = scanner){
			long count = 0;
			while(scanner.advance()){
				++count;
			}
			return count;
		}
	}

	public static <T> Optional<T> findAny(Scanner<T> scanner){
		try(Scanner<T> ref = scanner){
			if(scanner.advance()){
				return Optional.of(scanner.current());
			}
			return Optional.empty();
		}
	}

	public static <T> Optional<T> findFirst(Scanner<T> scanner){
		try(Scanner<T> ref = scanner){
			if(scanner.advance()){
				return Optional.of(scanner.current());
			}
			return Optional.empty();
		}
	}

	public static <T> Optional<T> findLast(Scanner<T> scanner){
		try(Scanner<T> ref = scanner){
			T last = null;
			while(scanner.advance()){
				last = scanner.current();
			}
			return Optional.ofNullable(last);
		}
	}

	public static <T> void forEach(Scanner<T> scanner, Consumer<? super T> action){
		try(Scanner<T> ref = scanner){
			while(scanner.advance()){
				action.accept(scanner.current());
			}
		}
	}

	public static <T> boolean hasAny(Scanner<T> scanner){
		try(Scanner<T> ref = scanner){
			return scanner.advance();
		}
	}

	public static <T> boolean isEmpty(Scanner<T> scanner){
		try(Scanner<T> ref = scanner){
			return !scanner.advance();
		}
	}

	public static <T> ArrayList<T> list(Scanner<T> scanner){
		try(Scanner<T> ref = scanner){
			ArrayList<T> list = new ArrayList<>();
			while(scanner.advance()){
				list.add(scanner.current());
			}
			return list;
		}
	}

	public static <T> Optional<T> max(Scanner<T> scanner, Comparator<? super T> comparator){
		try(Scanner<T> ref = scanner){
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
		try(Scanner<T> ref = scanner){
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

	public static <T> Stream<T> nativeStream(Scanner<T> scanner){
		Spliterator<T> spliterator = spliterator(scanner);
		Stream<T> stream = StreamSupport.stream(spliterator, false);
		stream.onClose(() -> scanner.close());
		return stream;
	}

	public static <T> boolean noneMatch(Scanner<T> scanner, Predicate<? super T> predicate){
		try(Scanner<T> ref = scanner){
			while(scanner.advance()){
				if(predicate.test(scanner.current())){
					return false;
				}
			}
			return true;
		}
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

	public static <T> List<T> take(Scanner<T> scanner, int numToTake){
		List<T> items = new ArrayList<>();
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
		try(Scanner<?> ref = scanner){
			return list(scanner).toArray();
		}
	}

}

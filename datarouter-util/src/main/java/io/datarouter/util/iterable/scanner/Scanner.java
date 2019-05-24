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
package io.datarouter.util.iterable.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.datarouter.util.iterable.scanner.iterable.IterableScanner;
import io.datarouter.util.iterable.scanner.iterable.IteratorScanner;
import io.datarouter.util.iterable.scanner.iterable.ScannerIterator;

//Alternative to Iterator when it is hard to do hasNext().  Suitable for high performance.
public interface Scanner<T> extends Iterable<T>{

	T getCurrent();

	/*
	 * Return true if the current value was advanced, otherwise false.  Repeated calls after the initial false should
	 * continue to return false without side effects.
	 */
	boolean advance();

	default long count(){
		long count = 0;
		while(advance()){
			++count;
		}
		return count;
	}

	default BatchScanner<T> batch(int batchSize){
		return new BatchingScanner<>(this, batchSize);
	}

	/**
	 * Removes consecutive duplicates.  Lighter weight than Stream's distinct() because all elements need not be
	 * collected into memory.
	 */
	default Scanner<T> deduplicate(){
		return new DeduplicatingScanner<>(this);
	}

	default Scanner<T> exclude(Predicate<? super T> predicate){
		return new ExcludeScanner<>(this, predicate);
	}

	default Scanner<T> include(Predicate<? super T> predicate){
		return new IncludeScanner<>(this, predicate);
	}

	default Scanner<T> interrupt(Supplier<Boolean> interruptor){
		return new InterruptibleScanner<>(this, interruptor);
	}

	@Override
	default Iterator<T> iterator(){
		return new ScannerIterator<>(this);
	}

	default Scanner<T> limit(long limit){
		return new LimitingScanner<>(this, limit);
	}

	default ArrayList<T> list(){
		ArrayList<T> result = new ArrayList<>();
		while(advance()){
			result.add(getCurrent());
		}
		return result;
	}

	default <R> Scanner<R> map(Function<? super T, ? extends R> mapper){
		return new MappingScanner<>(this, mapper);
	}

	default <R> ScannerScanner<R> mapToScanner(Function<? super T,Scanner<R>> mapper){
		Scanner<Scanner<R>> scannerOfScanners = map(mapper);
		return new ScannerScanner<>(scannerOfScanners);
	}

	default Scanner<T> skip(long numToSkip){
		long numSkipped = 0;
		while(numSkipped < numToSkip && advance()){
			++numSkipped;
		}
		return this;
	}

	default Scanner<T> peek(Consumer<? super T> consumer){
		return new PeekingScanner<>(this, consumer);
	}

	@Override
	default Spliterator<T> spliterator(){
		return Spliterators.spliteratorUnknownSize(iterator(), 0);
	}

	default Stream<T> stream(){
		return StreamSupport.stream(spliterator(), false);
	}

	/*----------- static --------------*/

	public static <T> Scanner<T> empty(){
		return new EmptyScanner<>();
	}

	@SafeVarargs
	public static <T> Scanner<T> of(T... inputs){
		return new IterableScanner<>(Arrays.asList(inputs));
	}

	public static <T> Scanner<T> of(Iterator<T> iterator){
		return new IteratorScanner<>(iterator);
	}

	public static <T> Scanner<T> of(Iterable<T> iterable){
		return new IterableScanner<>(iterable);
	}

	public static <T> Scanner<T> of(Stream<T> stream){
		return new IteratorScanner<>(stream.iterator());
	}

}

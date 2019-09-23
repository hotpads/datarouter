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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface Scanner<T> extends Iterable<T>, AutoCloseable{

	boolean advance();

	T current();

	@Override
	default void close(){
	}

	default boolean allMatch(Predicate<? super T> predicate){
		return ScannerTool.allMatch(this, predicate);
	}

	default boolean anyMatch(Predicate<? super T> predicate){
		return ScannerTool.anyMatch(this, predicate);
	}

	default long count(){
		return ScannerTool.count(this);
	}

	default Optional<T> findAny(){
		return ScannerTool.findAny(this);
	}

	default Optional<T> findFirst(){
		return ScannerTool.findFirst(this);
	}

	default Optional<T> findLast(){
		return ScannerTool.findLast(this);
	}

	@Override
	default void forEach(Consumer<? super T> action){
		ScannerTool.forEach(this, action);
	}

	default boolean hasAny(){
		return ScannerTool.hasAny(this);
	}

	default boolean isEmpty(){
		return ScannerTool.isEmpty(this);
	}

	default List<T> list(){
		return ScannerTool.list(this);
	}

	default Optional<T> max(Comparator<? super T> comparator){
		return ScannerTool.max(this, comparator);
	}

	default Optional<T> min(Comparator<? super T> comparator){
		return ScannerTool.min(this, comparator);
	}

	default boolean noneMatch(Predicate<? super T> predicate){
		return ScannerTool.noneMatch(this, predicate);
	}

	default Scanner<T> skip(long numToSkip){
		return ScannerTool.skip(this, numToSkip);
	}

	default List<T> take(int numToTake){
		return ScannerTool.take(this, numToTake);
	}

	default Object[] toArray(){
		return ScannerTool.toArray(this);
	}

	/**
	 * Stop the scanner when the predicate matches, excluding the item that caused it to stop.
	 */
	default Scanner<T> advanceUntil(Predicate<? super T> predicate){
		return new AdvanceUntilScanner<>(this, predicate);
	}

	/**
	 * Stop the scanner when the predicated fails to match, excluding the item that caused it to stop.
	 */
	default Scanner<T> advanceWhile(Predicate<? super T> predicate){
		return new AdvanceWhileScanner<>(this, predicate);
	}

	default Scanner<List<T>> batch(int batchSize){
		return new BatchingScanner<>(this, batchSize);
	}

	default Scanner<T> deduplicate(){
		return new DeduplicatingScanner<>(this);
	}

	default Scanner<T> distinct(){
		return new DistinctScanner<>(this);
	}

	default Scanner<T> exclude(Predicate<? super T> predicate){
		return new FilteringScanner<>(this, predicate.negate());
	}

	default Scanner<T> include(Predicate<? super T> predicate){
		return new FilteringScanner<>(this, predicate);
	}

	default Scanner<T> limit(long limit){
		return new LimitingScanner<>(this, limit);
	}

	default <R> Scanner<R> map(Function<? super T, ? extends R> mapper){
		return new MappingScanner<>(this, mapper);
	}

	@SuppressWarnings("resource")
	default Scanner<T> prefetch(ExecutorService exec, int batchSize){
		return new PrefetchingScanner<>(this, exec, batchSize)
				.mapToScanner(Scanner::of)
				.concatenate();
	}

	default Scanner<T> peek(Consumer<? super T> consumer){
		return new PeekingScanner<>(this, consumer);
	}

	default Scanner<T> sorted(){
		return new NaturalSortingScanner<>(this);
	}

	default Scanner<T> sorted(Comparator<? super T> comparator){
		return new SortingScanner<>(this, comparator);
	}

	default Scanner<T> step(long stepSize){
		return new SteppingScanner<>(this, stepSize);
	}

	/*--------------------------- ScannerScanner ----------------------------*/

	default <R> ScannerScanner<R> mapToScanner(Function<? super T,Scanner<R>> mapper){
		return new ScannerScanner<>(map(mapper));
	}

	/*----------------------------- Parallel --------------------------------*/

	default ParallelScanner<T> parallel(ParallelScannerContext context){
		return new ParallelScanner<>(context, this);
	}

	/*----------------------------- Iterator --------------------------------*/

	@Override
	default Iterator<T> iterator(){
		return new ScannerIterator<>(this);
	}

	/*----------------------------- Stream ----------------------------------*/

	default <R,A> R collect(Collector<? super T,A,R> collector){
		return stream().collect(collector);
	}

	default Optional<T> reduce(BinaryOperator<T> accumulator){
		return stream().reduce(accumulator);
	}

	default Stream<T> stream(){
		return new ScannerStream<>(this);
	}

	/*----------------------------- create ----------------------------------*/

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
		return new StreamScanner<>(stream);
	}

}

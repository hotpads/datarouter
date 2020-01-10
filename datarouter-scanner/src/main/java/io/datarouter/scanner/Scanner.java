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

import java.io.Closeable;
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

public interface Scanner<T> extends Closeable{

	boolean advance();

	T current();

	@Override
	default void close(){
	}

	/*--------------------------- terminal ops ----------------------------*/

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

	default Object[] toArray(){
		return ScannerTool.toArray(this);
	}

	/*--------------------------- intermediate ops ----------------------------*/

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
		return new DeduplicatingScanner<>(this, Function.identity());
	}

	default Scanner<T> deduplicate(Function<T,?> keyExtractor){
		return new DeduplicatingScanner<>(this, keyExtractor);
	}

	default Scanner<T> distinct(){
		return new DistinctScanner<>(this);
	}

	default Scanner<T> each(Consumer<? super T> consumer){
		return new EachScanner<>(this, consumer);
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

	default <R> Scanner<R> link(Function<Scanner<T>,BaseLinkedScanner<T,R>> scannerBuilder){
		return scannerBuilder.apply(this);
	}

	default <R> Scanner<R> map(Function<? super T, ? extends R> mapper){
		return new MappingScanner<>(this, mapper);
	}

	default Scanner<RetainingGroup<T>> retain(int retaining){
		return new RetainingScanner<>(this, retaining);
	}

	@SuppressWarnings("resource")
	default Scanner<T> prefetch(ExecutorService exec, int batchSize){
		return new PrefetchingScanner<>(this, exec, batchSize)
				.mapToScanner(Scanner::of)
				.concatenate();
	}

	default Scanner<T> sample(long sampleSize, boolean includeLast){
		return new SamplingScanner<>(this, sampleSize, includeLast);
	}

	default Scanner<T> skip(long numToSkip){
		return ScannerTool.skip(this, numToSkip);
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

	default List<T> take(int numToTake){
		return ScannerTool.take(this, numToTake);
	}

	/*--------------------------- ScannerScanner ----------------------------*/

	default <R> ScannerScanner<R> mapToScanner(Function<? super T,Scanner<R>> mapper){
		return new ScannerScanner<>(map(mapper));
	}

	default <R> Scanner<R> collate(Function<? super T,Scanner<R>> mapper){
		return mapToScanner(mapper).collate();
	}

	default <R> Scanner<R> collate(Function<? super T,Scanner<R>> mapper, Comparator<? super R> comparator){
		return mapToScanner(mapper).collate(comparator);
	}

	default <R> Scanner<R> concatenate(Function<? super T,Scanner<R>> mapper){
		return mapToScanner(mapper).concatenate();
	}

	/*----------------------------- Parallel --------------------------------*/

	default ParallelScanner<T> parallel(ParallelScannerContext context){
		return new ParallelScanner<>(context, this);
	}

	/*----------------------------- Iterator --------------------------------*/

	default Iterator<T> iterator(){
		return new ScannerIterator<>(this);
	}

	default Iterable<T> iterable(){
		return this::iterator;
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

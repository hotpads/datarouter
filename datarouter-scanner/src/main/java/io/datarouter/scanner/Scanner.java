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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
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

	default <C extends Collection<T>> C collect(Supplier<C> collectionSupplier){
		return ScannerTool.collect(this, collectionSupplier);
	}

	default <R,A> R collect(Collector<? super T,A,R> collector){
		return stream().collect(collector);
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

	default Scanner<T> flush(Consumer<List<T>> consumer){
		return ScannerTool.flush(this, consumer);
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

	default <R> R listTo(Function<List<T>,R> mapper){
		return mapper.apply(list());
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

	default Optional<T> reduce(BinaryOperator<T> reducer){
		return ScannerTool.reduce(this, reducer);
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

	/**
	 * Removes consecutive duplicates. Lighter weight than distinct() because all elements need not be collected into
	 * memory.
	 */
	default Scanner<T> deduplicate(){
		return new DeduplicatingScanner<>(this, Function.identity());
	}

	default Scanner<T> deduplicateBy(Function<T,?> mapper){
		return new DeduplicatingScanner<>(this, mapper);
	}

	default Scanner<T> distinct(){
		return new DistinctScanner<>(this, Function.identity());
	}

	default Scanner<T> distinctBy(Function<T,?> mapper){
		return new DistinctScanner<>(this, mapper);
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
				.concat(Scanner::of);
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

	default List<T> take(int numToTake){
		return ScannerTool.take(this, numToTake);
	}

	/*--------------------------- Scanner of Scanners ----------------------------*/

	@SuppressWarnings("unchecked")
	default <R> Scanner<R> collate(Function<? super T,Scanner<R>> mapper){
		return collate(mapper, (Comparator<? super R>)Comparator.naturalOrder());
	}

	default <R> Scanner<R> collate(Function<? super T,Scanner<R>> mapper, Comparator<? super R> comparator){
		List<Scanner<R>> scanners = map(mapper).list();
		if(scanners.size() == 1){
			return scanners.get(0);
		}
		return new CollatingScanner<>(scanners, comparator);
	}

	default <R> Scanner<R> concat(Function<? super T,Scanner<R>> mapper){
		Scanner<Scanner<R>> scanners = map(mapper);
		return new ConcatenatingScanner<>(scanners);
	}

	@SafeVarargs
	public static <T> Scanner<T> concat(Scanner<T>... scanners){
		return Scanner.of(scanners).concat(Function.identity());
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

	/*----------------------------- Stream --------------------------------*/

	default Stream<T> stream(){
		return new ScannerStream<>(this);
	}

	default IntStream streamInts(ToIntFunction<? super T> mapper){
		return stream().mapToInt(mapper);
	}

	default LongStream streamLongs(ToLongFunction<? super T> mapper){
		return stream().mapToLong(mapper);
	}

	default DoubleStream streamDoubles(ToDoubleFunction<? super T> mapper){
		return stream().mapToDouble(mapper);
	}

	/*----------------------------- create ----------------------------------*/

	public static <T> Scanner<T> empty(){
		return EmptyScanner.singleton();
	}

	public static <T> Scanner<T> of(T object){
		return ObjectScanner.of(object);
	}

	@SafeVarargs
	public static <T> Scanner<T> of(T... array){
		return ArrayScanner.of(array);
	}

	public static <T> Scanner<T> of(Iterator<T> iterator){
		return IteratorScanner.of(iterator);
	}

	public static <T> Scanner<T> of(Iterable<T> iterable){
		return IterableScanner.of(iterable);
	}

	public static <T> Scanner<T> of(Stream<T> stream){
		return StreamScanner.of(stream);
	}

}

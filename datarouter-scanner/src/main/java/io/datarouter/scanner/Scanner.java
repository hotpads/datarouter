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
import java.util.Map;
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
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import io.datarouter.scanner.ScannerToMap.Replace;

/**
 * A form of iterator that operates as lazily as possible, not knowing if the next item is available until advancing and
 * dropping the reference to the previous item. Default interface methods are included so Scanners can be assembled into
 * a pipeline before advancing through the items.
 *
 * Similar to Iterator or Stream, a Scanner can only be consumed once.
 */
public interface Scanner<T> extends Closeable{

	/**
	 * Try to update current to the next item, if there is one.
	 *
	 * @return  True if it advanced
	 */
	boolean advance();

	/**
	 * @return  The current item, only valid if advance() returned true
	 */
	T current();

	/**
	 * Override to cleanup any resources. The call should be propagated to all parent scanners.
	 *
	 * In the infrequent case of calling advance/current in an application, the Scanner should be explicitly closed.
	 * Because the included Scanner operations will close themselves when they fail to advance, it frequently a no-op
	 * for the application, unless closing the scanner before it stops advancing.
	 */
	@Override
	default void close(){
	}

	/**
	 * Perform an operation on each item.
	 *
	 * @param action  Consumer::accept is performed on each item
	 */
	default void forEach(Consumer<? super T> action){
		ScannerTool.forEach(this, action);
	}

	/**
	 * Consume up to N items without closing, only closing the scanner if the last item was consumed.
	 *
	 * @param numToTake  Maximum returned items
	 * @return  List with up to numToTake items
	 */
	default List<T> take(int numToTake){
		return ScannerTool.take(this, numToTake);
	}

	/*----------------------------- Create ----------------------------------*/

	/**
	 * @return  A scanner that immediately returns advance=false
	 */
	public static <T> Scanner<T> empty(){
		return EmptyScanner.singleton();
	}

	/**
	 * @param supplier  Supplier that generates items indefinitely
	 * @return  A scanner that advances through the items
	 */
	public static <T> Scanner<T> generate(Supplier<T> supplier){
		return new GeneratingScanner<>(supplier);
	}

	/**
	 * Generate a sequence where each item is calculated off the one before it.
	 *
	 * @param seed  The first item
	 * @param unaryOperator  A function applied to the current item to generate the next item
	 * @return  A scanner that advances through the items
	 */
	public static <T> Scanner<T> iterate(T seed, UnaryOperator<T> unaryOperator){
		return new IteratingScanner<>(seed, unaryOperator);
	}

	/**
	 * Convert an Object into a Scanner if non-null.
	 *
	 * @param object  A nullable Object
	 * @return  An empty scanner if the Object was null, otherwise a single-item Scanner with the Object
	 */
	public static <T> Scanner<T> ofNullable(T object){
		return ObjectScanner.ofNullable(object);
	}

	/**
	 * Convert an Object into a Scanner.
	 *
	 * @param object  A non-null Object
	 * @return  A single-item Scanner with the Object
	 */
	public static <T> Scanner<T> of(T object){
		return ObjectScanner.of(object);
	}

	/**
	 * Create a Scanner of items in the array.
	 *
	 * @param array  An array or var-args
	 * @return  A Scanner that visits each item in the array
	 */
	@SafeVarargs
	public static <T> Scanner<T> of(T... array){
		return ArrayScanner.of(array);
	}

	/**
	 * Create a Scanner of items in the Iterator.
	 *
	 * @param iterator  An Iterator
	 * @return  A Scanner that visits each item in the Iterator
	 */
	public static <T> Scanner<T> of(Iterator<T> iterator){
		return IteratorScanner.of(iterator);
	}

	/**
	 * Create a Scanner of items in the Iterable.
	 *
	 * @param iterable  An Iterable, which includes any Collection
	 * @return  A Scanner that visits each item in the Iterable
	 */
	public static <T> Scanner<T> of(Iterable<T> iterable){
		return IterableScanner.of(iterable);
	}

	/**
	 * Create a Scanner of items in the Stream.
	 *
	 * @param stream  A Stream
	 * @return  A Scanner that visits each item in the Stream
	 */
	public static <T> Scanner<T> of(Stream<T> stream){
		return StreamScanner.of(stream);
	}

	/*--------------------------- Concat ----------------------------*/

	/**
	 * Combine the items from multiple Iterables into a single Scanner.  Use Function.identity() if they're already
	 * Iterables.
	 *
	 * @param mapToIterable  Converts the input items into the Iterables to be combined
	 * @return  Scanner containing the items from all input Iterables, starting with the first
	 */
	default <R> Scanner<R> concatIter(Function<? super T,Iterable<R>> mapToIterable){
		Scanner<Iterable<R>> iterables = map(mapToIterable);
		Scanner<Scanner<R>> scanners = iterables.map(IterableScanner::of);
		return new ConcatenatingScanner<>(scanners);
	}

	/**
	 * Combine the items from multiple Scanners into a single Scanner.  Use Function.identity() if they're already
	 * Scanners.
	 *
	 * @param mapToScanner  Converts the input items into the Scanners to be combined
	 * @return  Scanner containing the items from all input Scanners, starting with the first
	 */
	default <R> Scanner<R> concat(Function<? super T,Scanner<R>> mapToScanner){
		Scanner<Scanner<R>> scanners = map(mapToScanner);
		return new ConcatenatingScanner<>(scanners);
	}

	/**
	 * Combine the items from multiple Scanners into a single Scanner.
	 *
	 * @param scanners  Input Scanners to be combined
	 * @return  Scanner containing the items from all input Scanners, starting with the first
	 */
	@SafeVarargs
	public static <T> Scanner<T> concat(Scanner<T>... scanners){
		return Scanner.of(scanners).concat(Function.identity());
	}

	/**
	 * Combine the items from multiple Iterables into a single Scanner.
	 *
	 * @param iterables  Input Iterables to be combined, where Iterable includes any Collection
	 * @return  Scanner containing the items from all input Iterables, starting with the first
	 */
	@SafeVarargs
	public static <T> Scanner<T> concat(Iterable<T>... iterables){
		return Scanner.of(iterables).concat(Scanner::of);
	}

	/*----------------------------- Append ----------------------------------*/

	/**
	 * Concats the provided Scanner after the current Scanner.
	 */
	default Scanner<T> append(Scanner<T> scanner){
		return concat(this, scanner);
	}

	/**
	 * Concats the provided array items after the current Scanner.
	 */
	default Scanner<T> append(@SuppressWarnings("unchecked") T... items){
		return concat(this, Scanner.of(items));
	}

	/**
	 * Concats the provided Iterable items after the current Scanner.
	 */
	default Scanner<T> append(Iterable<T> iterable){
		return concat(this, Scanner.of(iterable));
	}

	/*--------------------------- Collate ----------------------------*/

	/**
	 * Similar to the merge phase of a merge sort, assuming the input Scanners are sorted. Converts the input items
	 * to Scanners and feeds all Scanners through a PriorityQueue with "natural" comparator ordering.
	 *
	 * @return  Assuming input scanners are sorted, a single Scanner of all items in sorted order.
	 */
	@SuppressWarnings("unchecked")
	default <R> Scanner<R> collate(Function<? super T,Scanner<R>> mapper){
		return collate(mapper, (Comparator<? super R>)Comparator.naturalOrder());
	}

	/**
	 * Similar to the merge phase of a merge sort, assuming the input Scanners are sorted. Converts the input items
	 * to Scanners and feeds all Scanners through a PriorityQueue with the provided comparator.
	 *
	 * @return  Assuming input scanners are sorted according to the comparator, a single Scanner of all items in sorted
	 *         order.
	 */
	default <R> Scanner<R> collate(Function<? super T,Scanner<R>> mapper, Comparator<? super R> comparator){
		List<Scanner<R>> scanners = map(mapper).list();
		if(scanners.size() == 1){
			return scanners.get(0);
		}
		return new CollatingScanner<>(scanners, comparator);
	}

	/*--------------------------- Chain ----------------------------*/

	/**
	 * A caller can extend BaseLinkedScanner, which has exception handling logic, and fluently include it in the
	 * Scanner pipeline.
	 *
	 * @param scannerBuilder  Function to build the BaseLinkedScanner
	 */
	default <R> Scanner<R> link(Function<Scanner<T>,BaseLinkedScanner<T,R>> scannerBuilder){
		return scannerBuilder.apply(this);
	}

	/**
	 * Beta: Apply the provided Function which returns another Scanner. The other Scanner is now responsible for
	 * consuming the Scanner, or returning a continued Scanner.
	 *
	 * @param function A method reference that accepts this Scanner
	 */
	default <R> R apply(Function<Scanner<T>,R> function){
		return function.apply(this);
	}

	/**
	 * Beta: Pass the current Scanner to a method that will Consume this Scanner and return nothing.
	 *
	 * @param consumer  A method reference that accepts this Scanner
	 */
	default void then(Consumer<Scanner<T>> consumer){
		consumer.accept(this);
	}

	/*----------------------------- Multi-Threaded --------------------------------*/

	default ParallelScanner<T> parallel(ParallelScannerContext context){
		return new ParallelScanner<>(context, this);
	}

	@SuppressWarnings("resource")
	default Scanner<T> prefetch(ExecutorService exec, int batchSize){
		return new PrefetchingScanner<>(this, exec, batchSize)
				.concat(Scanner::of);
	}

	/*--------------------------- Intermediate ops ----------------------------*/

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
	 * Skips consecutive duplicates. Lighter weight than distinct() because all elements need not be collected into
	 * memory.
	 */
	default Scanner<T> deduplicateConsecutive(){
		return new DeduplicatingConsecutiveScanner<>(this, Function.identity());
	}

	/**
	 * Skips items where the mapper outputs the same value as the previous item. Lighter weight than distinctBy()
	 * because all elements need not be collected into memory.
	 */
	default Scanner<T> deduplicateConsecutiveBy(Function<T,?> mapper){
		return new DeduplicatingConsecutiveScanner<>(this, mapper);
	}

	/**
	 * Calls Consumer::accept on each item.
	 */
	default Scanner<T> each(Consumer<? super T> consumer){
		return new EachScanner<>(this, consumer);
	}

	/**
	 * Skips items where the Predicate returns true.
	 */
	default Scanner<T> exclude(Predicate<? super T> predicate){
		return new FilteringScanner<>(this, predicate.negate());
	}

	/**
	 * Skips items where the Predicate returns false.
	 */
	default Scanner<T> include(Predicate<? super T> predicate){
		return new FilteringScanner<>(this, predicate);
	}

	/**
	 * Ends the Scanner when the limit has been reached.
	 */
	default Scanner<T> limit(long limit){
		return new LimitingScanner<>(this, limit);
	}

	/**
	 * For each input item, outputs the result of Function::apply.
	 */
	default <R> Scanner<R> map(Function<? super T,? extends R> mapper){
		return new MappingScanner<>(this, mapper);
	}

	/**
	 * For retaining a window of N previous items.
	 *
	 * @param retaining  The number of extra items to retain, in addition to the Scanner's default of zero.
	 * @return  A RetainingGroup allowing you to call peekBack(1), to get the previous current() value.  Calling
	 * 			peekBack with a value higher than the "retaining" param will cause an exception.
	 */
	default Scanner<RetainingGroup<T>> retain(int retaining){
		return new RetainingScanner<>(this, retaining);
	}

	/**
	 * Return every Nth item.
	 *
	 * @param sampleSize  A Scanner with 8 items and sample size 8 will return either 2 or 3 results.
	 * @param includeLast  Whether to include the last item in case of a partial sample.
	 * @return  A Scanner of the sampled items.
	 */
	default Scanner<T> sample(long sampleSize, boolean includeLast){
		return new SamplingScanner<>(this, sampleSize, includeLast);
	}

	/**
	 * Skip the leading items from the Scanner.
	 *
	 * @param numToSkip  Skips up to this many items, or fewer if the Scanner had fewer items.
	 * @return  A Scanner with the remaining items, or an empty Scanner if all items were dropped.
	 */
	default Scanner<T> skip(long numToSkip){
		return ScannerTool.skip(this, numToSkip);
	}

	/**
	 * Applies the Function to each item in the Scanner, returning a new Scanner each time the mapped value changes.
	 *
	 * Similar to groupBy on an unbounded amount of data, but will result in multiple of the same groupings depending
	 * on the order of the input data.  Useful in the case of Scanning child objects and grouping by a parent.
	 */
	default Scanner<Scanner<T>> splitBy(Function<T,?> mapper){
		return new SplittingScanner<>(this, mapper);
	}

	/*--------------------------- Reducing ops ----------------------------*/

	/**
	 * Apply the Predicate to every item in the Scanner, returning whether they all match.
	 */
	default boolean allMatch(Predicate<? super T> predicate){
		return ScannerTool.allMatch(this, predicate);
	}

	/**
	 * Apply the predicate to each item in the Scanner until one matches, returning true, otherwise return false if the
	 * Scanner is consumed with no matches.
	 */
	default boolean anyMatch(Predicate<? super T> predicate){
		return ScannerTool.anyMatch(this, predicate);
	}

	/**
	 * Advance through every item in the Scanner, returning the count of items seen.
	 */
	default long count(){
		return ScannerTool.count(this);
	}

	/**
	 * Return any item encountered in the Scanner, wrapped in an Optional, otherwise Optional.empty() if no items
	 * were found.
	 */
	default Optional<T> findAny(){
		return findFirst();
	}

	/**
	 * Return the first item encountered in the Scanner, wrapped in an Optional, otherwise Optional.empty() if no items
	 * were found.
	 */
	default Optional<T> findFirst(){
		return ScannerTool.findFirst(this);
	}

	/**
	 * Advance through every item in the Scanner, returning the last item wrapped in an Optional, otherwise
	 * Optional.empty() if the Scanner had no items.
	 */
	default Optional<T> findLast(){
		return ScannerTool.findLast(this);
	}

	/**
	 * Advance through all items, retaining the maximum as computed by the Comparator and returning it.
	 */
	default Optional<T> findMax(Comparator<? super T> comparator){
		return ScannerTool.max(this, comparator);
	}

	/**
	 * Advance through all items, retaining the minimum as computed by the Comparator and returning it.
	 */
	default Optional<T> findMin(Comparator<? super T> comparator){
		return ScannerTool.min(this, comparator);
	}

	/**
	 * Test whether the first advance() returns true.
	 */
	default boolean hasAny(){
		return ScannerTool.hasAny(this);
	}

	/**
	 * Test whether the first advance() returns false.
	 */
	default boolean isEmpty(){
		return ScannerTool.isEmpty(this);
	}

	/**
	 * Advance through all items, retaining the maximum as computed by the Comparator and returning it.
	 */
	default Optional<T> max(Comparator<? super T> comparator){
		return findMax(comparator);
	}

	/**
	 * Advance through all items, retaining the minimum as computed by the Comparator and returning it.
	 */
	default Optional<T> min(Comparator<? super T> comparator){
		return findMin(comparator);
	}

	/**
	 * Return false as soon as the Predicate passes, otherwise true if all items fail the Predicate.
	 */
	default boolean noneMatch(Predicate<? super T> predicate){
		return ScannerTool.noneMatch(this, predicate);
	}

	default Optional<T> reduce(BinaryOperator<T> reducer){
		return ScannerTool.reduce(this, reducer);
	}

	default T reduce(T seed, BinaryOperator<T> reducer){
		return ScannerTool.reduce(this, seed, reducer);
	}

	/*--------------------------- Collecting ops ----------------------------*/

	default <C extends Collection<T>> C collect(Supplier<C> collectionSupplier){
		return ScannerTool.collect(this, collectionSupplier);
	}

	default <R,A> R collect(Collector<? super T,A,R> collector){
		return stream().collect(collector);
	}

	default Scanner<T> distinct(){
		return new DistinctScanner<>(this, Function.identity());
	}

	default Scanner<T> distinctBy(Function<T,?> mapper){
		return new DistinctScanner<>(this, mapper);
	}

	default Scanner<T> flush(Consumer<List<T>> consumer){
		return ScannerTool.flush(this, consumer);
	}

	default List<T> list(){
		return ScannerTool.list(this);
	}

	default <R> R listTo(Function<List<T>,R> mapper){
		return mapper.apply(list());
	}

	/**
	 * Collect all items into an List and return a Scanner that iterates through them backwards.
	 */
	default Scanner<T> reverse(){
		return listTo(ReverseListScanner::of);
	}

	default Scanner<T> shuffle(){
		return new ShufflingScanner<>(this);
	}

	default Scanner<T> sort(){
		return new NaturalSortingScanner<>(this);
	}

	default Scanner<T> sort(Comparator<? super T> comparator){
		return new SortingScanner<>(this, comparator);
	}

	default Scanner<T> sorted(){
		return sort();
	}

	default Scanner<T> sorted(Comparator<? super T> comparator){
		return sort(comparator);
	}

	default Object[] toArray(){
		return ScannerTool.toArray(this);
	}

	/*--------------------------- to HashMap ----------------------------*/

	default Map<T,T> toMap(){
		return apply(ScannerToMap.of());
	}

	default <K> Map<K,T> toMap(Function<T,K> keyFunction){
		return apply(ScannerToMap.of(keyFunction));
	}

	default <K,V> Map<K,V> toMap(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction){
		return apply(ScannerToMap.of(keyFunction, valueFunction));
	}

	default <K,V> Map<K,V> toMap(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Replace replacePolicy){
		return apply(ScannerToMap.of(keyFunction, valueFunction, replacePolicy));
	}

	default <K,V> Map<K,V> toMap(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			BinaryOperator<V> mergeFunction){
		return apply(ScannerToMap.of(keyFunction, valueFunction, mergeFunction));
	}

	/*--------------------------- to provided Map ----------------------------*/

	default <K,M extends Map<K,T>> M toMapSupplied(
			Function<T,K> keyFunction,
			Supplier<M> mapSupplier){
		return apply(ScannerToMap.ofSupplied(keyFunction, mapSupplier));
	}

	default <K,V,M extends Map<K,V>> M toMapSupplied(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Supplier<M> mapSupplier){
		return apply(ScannerToMap.ofSupplied(keyFunction, valueFunction, mapSupplier));
	}

	default <K,V,M extends Map<K,V>> M toMapSupplied(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Replace replacePolicy,
			Supplier<M> mapSupplier){
		return apply(ScannerToMap.ofSupplied(keyFunction, valueFunction, replacePolicy, mapSupplier));
	}

	default <K,V,M extends Map<K,V>> M toMapSupplied(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			BinaryOperator<V> mergeFunction,
			Supplier<M> mapSupplier){
		return apply(ScannerToMap.ofSupplied(keyFunction, valueFunction, mergeFunction, mapSupplier));
	}

	/*-------------------------- groupBy -------------------------------*/

	default <K> Map<K,List<T>> groupBy(Function<T,K> keyFunction){
		return apply(ScannerToGroups.of(keyFunction));
	}

	default <K,V> Map<K,List<V>> groupBy(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction){
		return apply(ScannerToGroups.of(keyFunction, valueFunction));
	}

	default <K,V,M extends Map<K,List<V>>> M groupBy(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Supplier<M> mapSupplier){
		return apply(ScannerToGroups.of(keyFunction, valueFunction, mapSupplier));
	}

	default <K,V,C extends Collection<V>,M extends Map<K,C>> M groupBy(
			Function<T,K> keyFunction,
			Function<T,V> valueFunction,
			Supplier<M> mapSupplier,
			Supplier<C> collectionSupplier){
		return apply(ScannerToGroups.of(keyFunction, valueFunction, mapSupplier, collectionSupplier));
	}

	/*----------------------- to Iterator / Stream --------------------------------*/

	default Iterator<T> iterator(){
		return new ScannerIterator<>(this);
	}

	default Iterable<T> iterable(){
		return this::iterator;
	}

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

}

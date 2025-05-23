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

import java.io.Closeable;
import java.time.Duration;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.BiPredicate;
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

import io.datarouter.scanner.BatchByMinSizeScanner.ScannerMinSizeBatch;
import io.datarouter.scanner.PrimitiveArrayScanner.BooleanArrayScanner;
import io.datarouter.scanner.PrimitiveArrayScanner.ByteArrayScanner;
import io.datarouter.scanner.PrimitiveArrayScanner.CharacterArrayScanner;
import io.datarouter.scanner.PrimitiveArrayScanner.DoubleArrayScanner;
import io.datarouter.scanner.PrimitiveArrayScanner.FloatArrayScanner;
import io.datarouter.scanner.PrimitiveArrayScanner.IntegerArrayScanner;
import io.datarouter.scanner.PrimitiveArrayScanner.LongArrayScanner;
import io.datarouter.scanner.PrimitiveArrayScanner.ShortArrayScanner;
import io.datarouter.scanner.ScannerToMap.Replace;
import io.datarouter.scanner.SplittingScanner.SplitKeyAndScanner;

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
	 * Consume 1 item without closing, only closing the scanner if the last item was consumed.
	 * Not safe to call concurrently from multiple threads.
	 */
	default ScannerNextItem<T> next(){
		return ScannerTool.next(this);
	}

	/**
	 * Consume up to N items without closing, only closing the scanner if the last item was consumed.
	 * Not safe to call concurrently from multiple threads.
	 *
	 * @param numToTake  Maximum returned items
	 * @return  List with up to numToTake items
	 */
	default List<T> take(int numToTake){
		return ScannerTool.take(this, numToTake);
	}

	/**
	 * Consume the first item (if present) and return a Scanner equivalent to the whole original Scanner (this) before
	 * the method call.
	 *
	 * The original Scanner (this) will be consumed, so the returned one should be used instead to avoid side effects.
	 *
	 * @param action called to consume first item, if present
	 * @return Scanner with equivalent state to this before the method was called
	 */
	default Scanner<T> peekFirst(Consumer<? super T> action){
		return ScannerTool.peekFirst(this, action);
	}

	/*----------------------------- Create ----------------------------------*/

	/**
	 * @return  A scanner that immediately returns advance=false
	 */
	static <T> Scanner<T> empty(){
		return EmptyScanner.singleton();
	}

	/**
	 * @param supplier  Supplier that generates items indefinitely
	 * @return  A scanner that advances through the items
	 */
	static <T> Scanner<T> generate(Supplier<T> supplier){
		return new GeneratingScanner<>(supplier);
	}

	/**
	 * Generate a sequence where each item is calculated off the one before it.
	 *
	 * @param seed  The first item
	 * @param unaryOperator  A function applied to the current item to generate the next item
	 * @return  A scanner that advances through the items
	 */
	static <T> Scanner<T> iterate(T seed, UnaryOperator<T> unaryOperator){
		return new IteratingScanner<>(seed, unaryOperator);
	}

	/**
	 * Convert an Object into a Scanner if non-null.
	 *
	 * @param object  A nullable Object
	 * @return  An empty scanner if the Object was null, otherwise a single-item Scanner with the Object
	 */
	static <T> Scanner<T> ofNullable(T object){
		return ObjectScanner.ofNullable(object);
	}

	/**
	 * Convert an Object into a Scanner.
	 *
	 * @param object  A non-null Object
	 * @return  A single-item Scanner with the Object
	 */
	static <T> Scanner<T> of(T object){
		return ObjectScanner.of(object);
	}

	/**
	 * Create a Scanner of items in the array.
	 *
	 * @param array  An array or var-args
	 * @return  A Scanner that visits each item in the array
	 */
	@SafeVarargs
	static <T> Scanner<T> of(T... array){
		return ArrayScanner.of(array);
	}

	/**
	 * Create a Scanner of items in the Iterator.
	 *
	 * @param iterator  An Iterator
	 * @return  A Scanner that visits each item in the Iterator
	 */
	static <T> Scanner<T> of(Iterator<T> iterator){
		return IteratorScanner.of(iterator);
	}

	/**
	 * Create a Scanner of items in the Iterable.
	 *
	 * @param iterable  An Iterable, which includes any Collection
	 * @return  A Scanner that visits each item in the Iterable
	 */
	static <T> Scanner<T> of(Iterable<T> iterable){
		return IterableScanner.of(iterable);
	}

	/**
	 * Create a Scanner of items in the Stream.
	 *
	 * @param stream  A Stream
	 * @return  A Scanner that visits each item in the Stream
	 */
	static <T> Scanner<T> of(Stream<T> stream){
		return StreamScanner.of(stream);
	}

	/**
	 * Create a Scanner of indexes present in the BitSet.
	 *
	 * @param bitSet  A BitSet
	 * @return  A Scanner returning an Integer for each present bit in the BitSet
	 */
	static Scanner<Integer> ofBits(BitSet bitSet){
		return ScannerTool.scanBits(bitSet);
	}

	/*------------------- Primitive Array -----------------------*/

	static Scanner<Boolean> ofArray(boolean[] values){
		return new BooleanArrayScanner(values);
	}

	static Scanner<Byte> ofArray(byte[] values){
		return new ByteArrayScanner(values);
	}

	static Scanner<Character> ofArray(char[] values){
		return new CharacterArrayScanner(values);
	}

	static Scanner<Short> ofArray(short[] values){
		return new ShortArrayScanner(values);
	}

	static Scanner<Integer> ofArray(int[] values){
		return new IntegerArrayScanner(values);
	}

	static Scanner<Float> ofArray(float[] values){
		return new FloatArrayScanner(values);
	}

	static Scanner<Long> ofArray(long[] values){
		return new LongArrayScanner(values);
	}

	static Scanner<Double> ofArray(double[] values){
		return new DoubleArrayScanner(values);
	}

	/*--------------------------- Concat ----------------------------*/

	/**
	 * Combine the items from multiple Iterables into a single Scanner.
	 * Use Function.identity() if they're already Iterables.
	 *
	 * @param toIterableFn  Converts the input items into the Iterables to be combined
	 * @return  Scanner containing the items from all input Iterables, starting with the first
	 */
	default <R> Scanner<R> concatIter(Function<? super T,Iterable<R>> toIterableFn){
		Scanner<Iterable<R>> iterables = map(toIterableFn);
		Scanner<Scanner<R>> scanners = iterables.map(IterableScanner::of);
		return new ConcatenatingScanner<>(scanners);
	}

	/**
	 * Ignores empty Optionals, and dereferences the remaining ones.
	 * The Optionals must be non-null, or else a NullPointerException will be thrown.
	 *
	 * @param toOptionalFn  Converts the input items into the Optionals to be combined
	 * @return  Scanner containing the values from the non-empty Optionals
	 */
	default <R> Scanner<R> concatOpt(Function<? super T,Optional<R>> toOptionalFn){
		return map(toOptionalFn)
				.include(Optional::isPresent)
				.map(Optional::orElseThrow);
	}

	/**
	 * Combine the items from multiple Scanners into a single Scanner.  Use Function.identity() if they're already
	 * Scanners.
	 *
	 * @param toScannerFn  Converts the input items into the Scanners to be combined
	 * @return  Scanner containing the items from all input Scanners, starting with the first
	 */
	default <R> Scanner<R> concat(Function<? super T,Scanner<R>> toScannerFn){
		Scanner<Scanner<R>> scanners = map(toScannerFn);
		return new ConcatenatingScanner<>(scanners);
	}

	/**
	 * Combine the items from multiple Scanners into a single Scanner.
	 *
	 * @param scanners  Input Scanners to be combined
	 * @return  Scanner containing the items from all input Scanners, starting with the first
	 */
	@SafeVarargs
	static <T> Scanner<T> concat(Scanner<T>... scanners){
		return Scanner.of(scanners).concat(Function.identity());
	}

	/**
	 * Combine the items from multiple Iterables into a single Scanner.
	 *
	 * @param iterables  Input Iterables to be combined, where Iterable includes any Collection
	 * @return  Scanner containing the items from all input Iterables, starting with the first
	 */
	@SafeVarargs
	static <T> Scanner<T> concat(Iterable<T>... iterables){
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
	default <R> Scanner<R> collate(Function<? super T,Scanner<R>> toScannerFn){
		return collate(toScannerFn, (Comparator<? super R>)Comparator.naturalOrder());
	}

	/**
	 * Similar to the merge phase of a merge sort, assuming the input Scanners are sorted. Converts the input items
	 * to Scanners and feeds all Scanners through a PriorityQueue with the provided comparator.
	 *
	 * @return  Assuming input scanners are sorted according to the comparator, a single Scanner of all items in sorted
	 *         order.
	 */
	default <R> Scanner<R> collate(Function<? super T,Scanner<R>> toScannerFn, Comparator<? super R> comparator){
		List<Scanner<R>> scanners = map(toScannerFn).list();
		if(scanners.size() == 1){
			return scanners.getFirst();
		}
		return new CollatingScanner<>(scanners, comparator);
	}

	/**
	 * New implementation with assumption that data is usually not random.
	 * TODO make the default
	 */
	default <R> Scanner<R> collateV2(Function<? super T,Scanner<R>> toScannerFn, Comparator<? super R> comparator){
		List<Scanner<R>> scanners = map(toScannerFn).list();
		if(scanners.size() == 1){
			return scanners.getFirst();
		}
		return new CollatingScannerV2<>(scanners, comparator);
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

	default ParallelScanner<T> parallelOrdered(Threads threads){
		return new ParallelScanner<>(this, threads, false);
	}

	default ParallelScanner<T> parallelUnordered(Threads threads){
		return new ParallelScanner<>(this, threads, true);
	}

	/**
	 * Convert input items to Scanners.
	 * Take the first available item from any of the input Scanners.
	 * Pulls from the first N Scanners at a time, avoiding initializing potentially many of them.
	 */
	default <R> Scanner<R> merge(Threads threads, Function<? super T,Scanner<R>> toScannerFn){
		Scanner<Scanner<R>> scanners = map(toScannerFn);
		return new MergingScanner<>(threads, scanners);
	}

	/**
	 * Use a background thread to move items from the input scanner to a queue with a maximum capacity.
	 * The input scanning thread is blocked when the queue is full.
	 * The result scanner is blocked when the queue is empty.
	 */
	default Scanner<T> prefetch(ExecutorService exec, int queueCapacity){
		return new PrefetchingScanner<>(this, exec, queueCapacity);
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

	/**
	 * Group items into batches with a max of N items per batch.
	 * The last batch will have between 1 and N items.
	 */
	default Scanner<List<T>> batch(int batchSize){
		return new BatchingScanner<>(this, batchSize);
	}

	/**
	 * Return a new List of T when the sum of extracted sizes matches or exceeds the minSize.
	 * The value returned by each sizeExtractor will be rounded down to the nearest long value.
	 */
	default Scanner<ScannerMinSizeBatch<T>> batchByMinSizeWithStats(long minSize, Function<T,Number> sizeExtractor){
		return new BatchByMinSizeScanner<>(this, minSize, sizeExtractor);
	}

	/**
	 * Convenience method to return only the batch items.
	 */
	default Scanner<List<T>> batchByMinSize(long minSize, Function<T,Number> sizeExtractor){
		return new BatchByMinSizeScanner<>(this, minSize, sizeExtractor)
				.map(ScannerMinSizeBatch::items);
	}

	/**
	 * Skips consecutive duplicates. Lighter weight than distinct() because all elements need not be collected into
	 * memory.
	 */
	default Scanner<T> deduplicateConsecutive(){
		return new DeduplicatingConsecutiveScanner<>(this, Function.identity(), Objects::equals);
	}

	/**
	 * Skips items where the mapper outputs the same value as the previous item when compared with the supplied
	 * equalsPredicate.
	 */
	default <R> Scanner<T> deduplicateConsecutiveBy(Function<T,R> mapper, BiPredicate<R,R> equalsPredicate){
		return new DeduplicatingConsecutiveScanner<>(this, mapper, equalsPredicate);
	}

	/**
	 * Skips items where the mapper outputs the same value as the previous item as compared with Objects::equals.
	 */
	default Scanner<T> deduplicateConsecutiveBy(Function<T,?> mapper){
		return new DeduplicatingConsecutiveScanner<>(this, mapper, Objects::equals);
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
	 * Calls Consumer::accept on every Nth item.
	 */
	default Scanner<T> periodic(long period, Consumer<? super T> consumer){
		return new CountingPeriodicScanner<>(this, period, consumer);
	}

	/**
	 * Calls Consumer::accept on after the period has passed since the last call.
	 * For emitting periodic events during a long-running Scanner.
	 */
	default Scanner<T> periodic(Duration period, Consumer<? super T> consumer){
		return new TimingPeriodicScanner<>(this, period, consumer);
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
	default <R> Scanner<SplitKeyAndScanner<R,T>> splitByWithSplitKey(
			Function<T,R> mapper,
			BiPredicate<R,R> equalsPredicate){
		return new SplittingScanner<>(this, mapper, equalsPredicate);
	}
	/**
	 * Convenience method calling splitBy with the most commonly used equalsPredicate, Objects::equals.
	 */
	default <R> Scanner<SplitKeyAndScanner<R,T>> splitByWithSplitKey(Function<T,R> mapper){
		return new SplittingScanner<>(this, mapper, Objects::equals);
	}

	/**
	 * Convenience method to return only the inner split scanners.
	 */
	default <R> Scanner<Scanner<T>> splitBy(Function<T,R> mapper){
		return new SplittingScanner<>(this, mapper, Objects::equals)
				.map(SplitKeyAndScanner::scanner);
	}

	/**
	 * Convenience method to avoid passing totalItems.
	 * Note this collects them into memory.
	 */
	default Scanner<T> stagger(Duration duration){
		List<T> items = list();
		return new StaggeringScanner<>(Scanner.of(items), items.size(), duration);
	}

	/**
	 * Try to release items at an even cadence over the duration.
	 * Empty scanner returns immediately.
	 * First item is returned immediately.
	 * It is essentially a rate limiter.
	 * If downstream processing is behind then it will release items immediately.
	 * If total items was underestimated then the final items may be released immediately.
	 */
	default Scanner<T> stagger(long totalItems, Duration duration){
		return new StaggeringScanner<>(this, totalItems, duration);
	}

	/**
	 * Count nanos waiting for inputScanner.advance().
	 */
	default Scanner<T> timeNanos(Consumer<Long> nanosConsumer){
		return new TimeNanosScanner<>(this, nanosConsumer);
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

	default int countInt(){
		return ScannerTool.countInt(this);
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
	 * Retains the max N items as defined by the comparator and returns a Scanner of them in descending order.
	 */
	default Scanner<T> maxN(Comparator<? super T> comparator, int num){
		return ScannerTool.maxNDesc(this, comparator, num);
	}

	/**
	 * Retains the min N items as defined by the comparator and returns a Scanner of them in ascending order.
	 */
	default Scanner<T> minN(Comparator<? super T> comparator, int num){
		return ScannerTool.minNAsc(this, comparator, num);
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

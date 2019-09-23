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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
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

/**
 * Wrapper around a Scanner to allow closing it without explicitly closing the Stream.  Some operations do release
 * a native Stream that must still be closed if it isn't fully consumed.
 */
public class ScannerStream<T> implements Stream<T>{

	private final Scanner<T> scanner;

	public ScannerStream(Scanner<T> scanner){
		this.scanner = scanner;
	}

	/*-------------- private -----------------*/

	private Stream<T> nativeStream(){
		return ScannerTool.nativeStream(scanner);
	}

	private <R> R closeAndReturn(R result){
		close();
		return result;
	}

	/*--------------- Stream ------------------*/

	@Override
	public Iterator<T> iterator(){
		return scanner.iterator();
	}

	@Override
	public Spliterator<T> spliterator(){
		return ScannerTool.spliterator(scanner);
	}

	@Override
	public boolean isParallel(){
		return false;
	}

	@Override
	public Stream<T> sequential(){
		return this;
	}

	@Override
	public Stream<T> parallel(){
		return this;
	}

	@Override
	public Stream<T> unordered(){
		return this;
	}

	@Override
	public Stream<T> onClose(Runnable closeHandler){
		return nativeStream().onClose(closeHandler);
	}

	@Override
	public void close(){
		scanner.close();
	}

	/*------- return native stream that must be closed ----------------*/

	@Override
	public IntStream mapToInt(ToIntFunction<? super T> mapper){
		return nativeStream().mapToInt(mapper);
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super T> mapper){
		return nativeStream().mapToLong(mapper);
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper){
		return nativeStream().mapToDouble(mapper);
	}

	/*-------------- chain to another ScannerStream ---------------*/

	@Override
	public Stream<T> distinct(){
		return scanner.distinct().stream();
	}

	@Override
	public Stream<T> filter(Predicate<? super T> predicate){
		return scanner.include(predicate).stream();
	}

	@Override
	public <R> Stream<R> map(Function<? super T,? extends R> mapper){
		Scanner<R> mappedScanner = scanner.map(mapper);
		return mappedScanner.stream();
	}

	@Override
	public Stream<T> peek(Consumer<? super T> action){
		return scanner.peek(action).stream();
	}

	@Override
	public Stream<T> limit(long limit){
		return scanner.limit(limit).stream();
	}

	@Override
	public Stream<T> skip(long numToSkip){
		return scanner.skip(numToSkip).stream();
	}

	@Override
	public Stream<T> sorted(){
		return scanner.sorted().stream();
	}

	@Override
	public Stream<T> sorted(Comparator<? super T> comparator){
		return scanner.sorted(comparator).stream();
	}

	/*----------- terminate using Scanner ------------*/

	@Override
	public boolean anyMatch(Predicate<? super T> predicate){
		return scanner.anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super T> predicate){
		return scanner.allMatch(predicate);
	}

	@Override
	public long count(){
		return scanner.count();
	}

	@Override
	public Optional<T> findAny(){
		return scanner.findAny();
	}

	@Override
	public Optional<T> findFirst(){
		return scanner.findFirst();
	}

	@Override
	public void forEach(Consumer<? super T> action){
		scanner.forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super T> action){
		scanner.forEach(action);
	}

	@Override
	public Optional<T> max(Comparator<? super T> comparator){
		return scanner.max(comparator);
	}

	@Override
	public Optional<T> min(Comparator<? super T> comparator){
		return scanner.min(comparator);
	}

	@Override
	public boolean noneMatch(Predicate<? super T> predicate){
		return scanner.noneMatch(predicate);
	}

	@Override
	public Object[] toArray(){
		return scanner.toArray();
	}

	/*-------------- closeAndReturn -----------------*/

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator){
		return closeAndReturn(nativeStream().toArray(generator));
	}

	@Override
	public T reduce(T identity, BinaryOperator<T> accumulator){
		return closeAndReturn(nativeStream().reduce(identity, accumulator));
	}

	@Override
	public Optional<T> reduce(BinaryOperator<T> accumulator){
		return closeAndReturn(nativeStream().reduce(accumulator));
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner){
		return closeAndReturn(nativeStream().reduce(identity, accumulator, combiner));
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R,? super T> accumulator, BiConsumer<R,R> combiner){
		return closeAndReturn(nativeStream().collect(supplier, accumulator, combiner));
	}

	@Override
	public <R,A> R collect(Collector<? super T,A,R> collector){
		return closeAndReturn(nativeStream().collect(collector));
	}

	/*-------------- flatMap auto-closes the scanner -----------*/

	@Override
	public <R> Stream<R> flatMap(Function<? super T,? extends Stream<? extends R>> mapper){
		return nativeStream().flatMap(mapper);
	}

	@Override
	public IntStream flatMapToInt(Function<? super T,? extends IntStream> mapper){
		return nativeStream().flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(Function<? super T,? extends LongStream> mapper){
		return nativeStream().flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super T,? extends DoubleStream> mapper){
		return nativeStream().flatMapToDouble(mapper);
	}

}
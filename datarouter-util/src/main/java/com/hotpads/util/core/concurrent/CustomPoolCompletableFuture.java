package com.hotpads.util.core.concurrent;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A completable future that will guarantee to execute asynchronous tasks in the given {@link Executor}. The standard
 * {@link CompletableFuture} has two versions of every asynchronous task submission method: one that accepts an executor
 * as an argument which will run the submitted task and one version that has no executor argument but instead run on the
 * JVM-wide {@link ForkJoinPool#commonPool()}. The use of {@link ForkJoinPool#commonPool()} is considered undesirable.
 * It is very easy to forget using the version that accepts your custom executor. This class, which wraps around the
 * standard {@link CompletableFuture}, will always use the executor you provide at one of the static factory methods
 * to run async tasks submitted by the version of methods that accepts no executor. (You can still use the other
 * version to run a given task in a different executor.)
 *
 * <h3>The Fine Prints</h3>
 *
 * This class implements all interfaces of {@link CompletableFuture} and most of its methods, while providing a few
 * convenient additional methods. Static factory method in {@link CompletableFuture} that do not accept an executor are
 * not provided in this class as there is no way to specify the desired executor. See the static factory methods
 * for other changes.
 *
 * For compatible monadic compose operations (e.g. combine, compose,) a runtime check is performed to ensure that only
 * only custom-pool-fixed completable futures, i.e. instances of this class, are used.
 *
 * @param <T> the type of the future
 * @see CompletableFuture
 */
public class CustomPoolCompletableFuture<T> implements CompletionStage<T>, Future<T>{

	private final CompletableFuture<T> delegate;
	private final Executor customExecutor;

	/******************************* static factory methods  **************************************/

	public static CustomPoolCompletableFuture<Void> allOf(Collection<CustomPoolCompletableFuture<?>> futures){
		return allOf(futures.toArray(new CustomPoolCompletableFuture<?>[futures.size()]));
	}

	/**
	 * Return a future which will run after all given futures are completed. If the list/array of futures can be empty,
	 * {@link CustomPoolCompletableFuture#allOf(Executor, CustomPoolCompletableFuture[])} should be used instead.
	 *
	 * @param futures an array of futures
	 * @return a future
	 * @throws IllegalArgumentException if the array of futures is empty.
	 */
	public static CustomPoolCompletableFuture<Void> allOf(CustomPoolCompletableFuture<?>... futures){
		if(futures.length == 0){
			throw new IllegalArgumentException("Empty future list");
		}
		return allOf(futures[0].customExecutor, futures);
	}

	public static CustomPoolCompletableFuture<Void> allOf(Executor executor, CustomPoolCompletableFuture<?>... futures){
		CompletableFuture<?>[] delegates = extractDelegates(futures);
		return new CustomPoolCompletableFuture<>(CompletableFuture.allOf(delegates), executor);
	}

	public static CustomPoolCompletableFuture<Object> anyOf(Collection<CustomPoolCompletableFuture<?>> futures){
		return anyOf(futures.toArray(new CustomPoolCompletableFuture<?>[futures.size()]));
	}

	/**
	 * Return a future which will run after any of the given futures are completed. If the list/array of futures can be
	 * empty, {@link CustomPoolCompletableFuture#allOf(Executor, CustomPoolCompletableFuture[])} should be used instead.
	 *
	 * @param futures an array of futures
	 * @return a future
	 * @throws IllegalArgumentException if the array of futures is empty.
	 */
	public static CustomPoolCompletableFuture<Object> anyOf(CustomPoolCompletableFuture<?>... futures){
		if(futures.length == 0){
			throw new IllegalArgumentException("Empty future list");
		}
		return anyOf(futures[0].customExecutor, futures);
	}

	protected static CustomPoolCompletableFuture<Object> anyOf(Executor executor,
			CustomPoolCompletableFuture<?>[] futures){
		CompletableFuture<?>[] delegates = extractDelegates(futures);
		return new CustomPoolCompletableFuture<>(CompletableFuture.anyOf(delegates), executor);
	}

	public static <U> CustomPoolCompletableFuture<U> completedFuture(U value, Executor executor){
		return new CustomPoolCompletableFuture<U>(CompletableFuture.completedFuture(value), executor);
	}

	public static CustomPoolCompletableFuture<Void> runAsync(Runnable runnable, Executor executor){
		return new CustomPoolCompletableFuture<>(CompletableFuture.runAsync(runnable, executor), executor);
	}

	public static <U> CustomPoolCompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor){
		return new CustomPoolCompletableFuture<U>(CompletableFuture.supplyAsync(supplier, executor), executor);
	}

	/******************************* constructor  **************************************/

	protected CustomPoolCompletableFuture(final CompletableFuture<T> delegate, Executor executor){
		if(executor == null){
			throw new IllegalArgumentException("executor cannot be null");
		}
		this.delegate = delegate;
		this.customExecutor = executor;
	}

	/******************************* private helpers  **************************************/

	protected static CompletableFuture<?>[] extractDelegates(
			final CustomPoolCompletableFuture<?>[] futures){
		return Arrays.stream(futures)
				.map(f -> f.delegate)
				.collect(Collectors.toList())
				.toArray(new CompletableFuture[futures.length]);
	}

	protected <U> CustomPoolCompletableFuture<U> wrap(CompletableFuture<U> delegate){
		return new CustomPoolCompletableFuture<>(delegate, customExecutor);
	}

	protected <U> CustomPoolCompletableFuture<U> ensure(CompletionStage<U> stage){
		return (CustomPoolCompletableFuture<U>)stage; // or ClassCastException
	}

	/******************************* wrapped interface methods **************************************/

	@Override
	public boolean isDone(){
		return delegate.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException{
		return delegate.get();
	}

	@Override
	public T get(final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException{
		return delegate.get(timeout, unit);
	}

	public T join(){
		return delegate.join();
	}

	public T getNow(final T valueIfAbsent){
		return delegate.getNow(valueIfAbsent);
	}

	public boolean complete(final T value){
		return delegate.complete(value);
	}

	public boolean completeExceptionally(final Throwable ex){
		return delegate.completeExceptionally(ex);
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> thenApply(final Function<? super T, ? extends U> fn){
		return wrap(delegate.thenApply(fn));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> thenApplyAsync(final Function<? super T, ? extends U> fn){
		return wrap(delegate.thenApplyAsync(fn, customExecutor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> thenApplyAsync(final Function<? super T, ? extends U> fn,
			final Executor executor){
		return wrap(delegate.thenApplyAsync(fn, executor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> thenAccept(final Consumer<? super T> action){
		return wrap(delegate.thenAccept(action));
	}

	@Override
	public CustomPoolCompletableFuture<Void> thenAcceptAsync(final Consumer<? super T> action){
		return wrap(delegate.thenAcceptAsync(action, customExecutor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> thenAcceptAsync(final Consumer<? super T> action, final Executor executor){
		return wrap(delegate.thenAcceptAsync(action, executor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> thenRun(final Runnable action){
		return wrap(delegate.thenRun(action));
	}

	@Override
	public CustomPoolCompletableFuture<Void> thenRunAsync(final Runnable action){
		return wrap(delegate.thenRunAsync(action, customExecutor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> thenRunAsync(final Runnable action, final Executor executor){
		return wrap(delegate.thenRunAsync(action, executor));
	}

	@Override
	public <U, V> CustomPoolCompletableFuture<V> thenCombine(final CompletionStage<? extends U> other,
			final BiFunction<? super T, ? super U, ? extends V> fn){
		return wrap(delegate.thenCombine(ensure(other), fn));
	}

	@Override
	public <U, V> CustomPoolCompletableFuture<V> thenCombineAsync(final CompletionStage<? extends U> other,
			final BiFunction<? super T, ? super U, ? extends V> fn){
		return wrap(delegate.thenCombineAsync(ensure(other), fn, customExecutor));
	}

	@Override
	public <U, V> CustomPoolCompletableFuture<V> thenCombineAsync(final CompletionStage<? extends U> other,
			final BiFunction<? super T, ? super U, ? extends V> fn, final Executor executor){
		return wrap(delegate.thenCombineAsync(ensure(other), fn, executor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<Void> thenAcceptBoth(final CompletionStage<? extends U> other,
			final BiConsumer<? super T, ? super U> action){
		return wrap(delegate.thenAcceptBoth(ensure(other), action));
	}

	@Override
	public <U> CustomPoolCompletableFuture<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other,
			final BiConsumer<? super T, ? super U> action){
		return wrap(delegate.thenAcceptBothAsync(ensure(other), action, customExecutor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other,
			final BiConsumer<? super T, ? super U> action, final Executor executor){
		return wrap(delegate.thenAcceptBothAsync(ensure(other), action, executor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> runAfterBoth(final CompletionStage<?> other, final Runnable action){
		return wrap(delegate.runAfterBoth(ensure(other), action));
	}

	@Override
	public CustomPoolCompletableFuture<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action){
		return wrap(delegate.runAfterBothAsync(ensure(other), action, customExecutor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action,
			final Executor executor){
		return wrap(delegate.runAfterBothAsync(ensure(other), action, executor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> applyToEither(final CompletionStage<? extends T> other,
			final Function<? super T, U> fn){
		return wrap(delegate.applyToEither(ensure(other), fn));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> applyToEitherAsync(final CompletionStage<? extends T> other,
			final Function<? super T, U> fn){
		return wrap(delegate.applyToEitherAsync(ensure(other), fn, customExecutor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> applyToEitherAsync(final CompletionStage<? extends T> other,
			final Function<? super T, U> fn,
			final Executor executor){
		return wrap(delegate.applyToEitherAsync(ensure(other), fn, executor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> acceptEither(final CompletionStage<? extends T> other,
			final Consumer<? super T> action){
		return wrap(delegate.acceptEither(ensure(other), action));
	}

	@Override
	public CustomPoolCompletableFuture<Void> acceptEitherAsync(final CompletionStage<? extends T> other,
			final Consumer<? super T> action){
		return wrap(delegate.acceptEitherAsync(ensure(other), action, customExecutor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> acceptEitherAsync(final CompletionStage<? extends T> other,
			final Consumer<? super T> action,
			final Executor executor){
		return wrap(delegate.acceptEitherAsync(ensure(other), action, executor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> runAfterEither(final CompletionStage<?> other, final Runnable action){
		return wrap(delegate.runAfterEither(ensure(other), action));
	}

	@Override
	public CustomPoolCompletableFuture<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action){
		return wrap(delegate.runAfterEitherAsync(ensure(other), action, customExecutor));
	}

	@Override
	public CustomPoolCompletableFuture<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action,
			final Executor executor){
		return wrap(delegate.runAfterEitherAsync(ensure(other), action, executor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> thenCompose(final Function<? super T, ? extends CompletionStage<U>> fn){
		return wrap(delegate.thenCompose(arg -> ensure(fn.apply(arg))));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> thenComposeAsync(
			final Function<? super T, ? extends CompletionStage<U>> fn){
		return wrap(delegate.thenComposeAsync(arg -> ensure(fn.apply(arg)), customExecutor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> thenComposeAsync(
			final Function<? super T, ? extends CompletionStage<U>> fn, final Executor executor){
		return wrap(delegate.thenComposeAsync(arg -> ensure(fn.apply(arg)), executor));
	}

	@Override
	public CustomPoolCompletableFuture<T> whenComplete(final BiConsumer<? super T, ? super Throwable> action){
		return wrap(delegate.whenComplete(action));
	}

	@Override
	public CustomPoolCompletableFuture<T> whenCompleteAsync(final BiConsumer<? super T, ? super Throwable> action){
		return wrap(delegate.whenCompleteAsync(action, customExecutor));
	}

	@Override
	public CustomPoolCompletableFuture<T> whenCompleteAsync(final BiConsumer<? super T, ? super Throwable> action,
			final Executor executor){
		return wrap(delegate.whenCompleteAsync(action, executor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> handle(final BiFunction<? super T, Throwable, ? extends U> fn){
		return wrap(delegate.handle(fn));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> handleAsync(final BiFunction<? super T, Throwable, ? extends U> fn){
		return wrap(delegate.handleAsync(fn, customExecutor));
	}

	@Override
	public <U> CustomPoolCompletableFuture<U> handleAsync(final BiFunction<? super T, Throwable, ? extends U> fn,
			final Executor executor){
		return wrap(delegate.handleAsync(fn, executor));
	}

	@Override
	public CustomPoolCompletableFuture<T> exceptionally(final Function<Throwable, ? extends T> fn){
		return wrap(delegate.exceptionally(fn));
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning){
		return delegate.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled(){
		return delegate.isCancelled();
	}

	public boolean isCompletedExceptionally(){
		return delegate.isCompletedExceptionally();
	}

	public void obtrudeValue(final T value){
		delegate.obtrudeValue(value);
	}

	public void obtrudeException(final Throwable ex){
		delegate.obtrudeException(ex);
	}

	public int getNumberOfDependents(){
		return delegate.getNumberOfDependents();
	}

	/**
	 * @throws UnsupportedOperationException this method is not supported
	 */
	@Override
	public CompletableFuture<T> toCompletableFuture(){
		throw new UnsupportedOperationException("toCompletableFuture is not supported");
	}

	@Override
	public String toString(){
		return delegate.toString();
	}
}

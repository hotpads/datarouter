package com.hotpads.datarouter.util.async;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.hotpads.util.core.stream.StreamTool;

/**
 * Ref is a layer of indirection around an object, accessed via the get() method.  It is just a Supplier with some
 * added utils.  These concise util methods convert Ref-like objects into Refs so that everything can implement the
 * Supplier interface.
 */
public interface Ref<T> extends Supplier<T>{

	static <T> List<T> getAll(Iterable<? extends Ref<T>> refs){
		return StreamTool.map(refs, Ref::get);
	}


	/*---------- Objects Refs ----------*/

	static <T> List<Ref<T>> ofEach(Iterable<T> objs){
		return StreamTool.stream(objs)
				.map(Ref::of)
				.collect(Collectors.toList());
	}

	static <T> Ref<T> of(T obj){
		return new SingletonRef<>(obj);
	}

	static class SingletonRef<T> implements Ref<T>{
		private final T object;

		private SingletonRef(T obj){
			this.object = obj;
		}

		@Override
		public T get(){
			return object;
		}
	}


	/*---------- Callable Refs ----------*/

	static <T> List<Ref<T>> ofEachCallable(Iterable<Callable<T>> callables){
		return StreamTool.stream(callables)
				.map(Ref::ofCallable)
				.collect(Collectors.toList());
	}

	static <T> Ref<T> ofCallable(Callable<T> callable){
		return new CallableRef<>(callable);
	}

	static class CallableRef<T> implements Ref<T>{
		private final Callable<T> callable;

		private CallableRef(Callable<T> callable){
			this.callable = callable;
		}

		@Override
		public T get(){
			try{
				return callable.call();
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}


	/*---------- Future Refs ----------*/

	static <T> List<Ref<T>> ofEachFuture(Iterable<Future<T>> futures){
		return StreamTool.stream(futures)
				.map(Ref::ofFuture)
				.collect(Collectors.toList());
	}

	static <T> Ref<T> ofFuture(Future<T> future){
		return new FutureRef<>(future);
	}

	static class FutureRef<T> implements Ref<T>{
		private final Future<T> future;

		private FutureRef(Future<T> future){
			this.future = future;
		}

		@Override
		public T get(){
			try{
				return future.get();
			}catch(InterruptedException | ExecutionException e){
				throw new RuntimeException(e);
			}
		}
	}


	/*---------- Provider Refs ----------*/

	static <T> List<Ref<T>> ofEachProvider(Iterable<Provider<T>> providers){
		return StreamTool.stream(providers)
				.map(Ref::ofProvider)
				.collect(Collectors.toList());
	}

	static <T> Ref<T> ofProvider(Provider<T> provider){
		return new ProviderRef<>(provider);
	}

	static class ProviderRef<T> implements Ref<T>{
		private final Provider<T> provider;

		private ProviderRef(Provider<T> provider){
			this.provider = provider;
		}

		@Override
		public T get(){
			return provider.get();
		}
	}
}
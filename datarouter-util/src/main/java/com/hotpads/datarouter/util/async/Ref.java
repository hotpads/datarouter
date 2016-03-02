package com.hotpads.datarouter.util.async;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.hotpads.util.core.stream.StreamTool;

public interface Ref<T> extends Supplier<T>{

	public static <T> List<T> getAll(Iterable<? extends Ref<T>> refs){
		return StreamTool.map(refs, Ref::get);
	}

	/************ Objects Refs **************/

	public static <T> List<Ref<T>> ofMulti(Iterable<T> objs){
		return StreamTool.stream(objs)
				.map(Ref::of)
				.collect(Collectors.toList());
	}

	public static <T> Ref<T> of(T obj){
		return new SingletonRef<>(obj);
	}

	public static class SingletonRef<T> implements Ref<T>{
		private final T object;

		public SingletonRef(T obj){
			this.object = obj;
		}

		@Override
		public T get(){
			return object;
		}
	}


	/************ Future Refs **************/

	public static <T> List<Ref<T>> ofFutures(Iterable<Future<T>> futures){
		return StreamTool.stream(futures)
				.map(Ref::ofFuture)
				.collect(Collectors.toList());
	}

	public static <T> Ref<T> ofFuture(Future<T> future){
		return new FutureRef<>(future);
	}

	public static class FutureRef<T> implements Ref<T>{
		private final Future<T> future;

		public FutureRef(Future<T> future){
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
}

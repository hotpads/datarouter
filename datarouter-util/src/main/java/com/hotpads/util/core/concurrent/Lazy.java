package com.hotpads.util.core.concurrent;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.hotpads.datarouter.util.async.Ref;

public abstract class Lazy<R> implements Callable<R>, Ref<R>{

	private volatile R value;

	/**
	 * @deprecated use {@link Lazy#of(Supplier)}
	 */
	@Deprecated
	public Lazy(){}

	//work done in load will only happen once
	protected abstract R load();

	//allow another thread to trigger the Lazy
	@Override
	public R call(){
		return get();
	}

	@Override
	public R get(){
		if(value != null){
			return value;
		}
		synchronized (this){
			if(value != null){
				return value;
			}
			return load();
		}
	}

	public static <R> Lazy<R> of(Supplier<R> supplier){
		return new LazyFunctional<>(supplier);
	}

	private static class LazyFunctional<R> extends Lazy<R>{

		private final Supplier<R> supplier;

		public LazyFunctional(Supplier<R> supplier){
			this.supplier = supplier;
		}

		@Override
		protected R load(){
			return supplier.get();
		}

	}

}

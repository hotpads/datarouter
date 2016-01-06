package com.hotpads.util.core.concurrent;

import java.util.function.Supplier;

public abstract class Lazy<R> {

	private volatile R value;

	/**
	 * @deprecated use {@link Lazy#of(Supplier)}
	 */
	@Deprecated
	public Lazy(){}

	protected abstract R load();

	public R get(){
		if(value != null){
			return value;
		}
		synchronized (this){
			if(value != null){
				return value;
			}
			return value = load();
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

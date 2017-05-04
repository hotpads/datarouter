package com.hotpads.util.core.concurrent;

import java.util.function.Supplier;

public abstract class Lazy<R> extends CheckedLazy<R,Exception> implements Supplier<R>{

	@Override
	public R get(){
		try{
			return super.get();
		}catch(RuntimeException e){
			throw e;
		}catch(Exception e){
			throw new RuntimeException(e);
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

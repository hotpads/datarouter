package io.datarouter.util.lazy;

import java.util.concurrent.Callable;

public abstract class CheckedLazy<R,E extends Exception> implements Callable<R>{

	private volatile R value;

	protected CheckedLazy(){}

	//work done in load will only happen once
	protected abstract R load() throws E;

	public R get() throws E{
		if(value != null){
			return value;
		}
		synchronized (this){
			if(value != null){
				return value;
			}
			value = load();
			return value;
		}
	}

	public boolean isInitialized(){
		return value != null;
	}

	public R getIfInitialized(){
		return value;
	}

	/*-------------- Callable -----------------*/

	//allow another thread to trigger the Lazy
	@Override
	public R call() throws E{
		return get();
	}

	/*----------- CheckedLazyFunctional ---------------*/

	public static <R, E extends Exception> CheckedLazy<R,E> ofChecked(CheckedSupplier<R,E> supplier){
		return new CheckedLazyFunctional<>(supplier);
	}

	public interface CheckedSupplier<R,E extends Exception>{
		R get() throws E;
	}

	private static class CheckedLazyFunctional<R,E extends Exception> extends CheckedLazy<R,E>{

		private final CheckedSupplier<R,E> supplier;

		public CheckedLazyFunctional(CheckedSupplier<R,E> supplier){
			this.supplier = supplier;
		}

		@Override
		protected R load() throws E{
			return supplier.get();
		}

	}

}

package io.datarouter.util.lazy;

import java.util.function.Supplier;

public abstract class Lazy<R> extends CheckedLazy<R,RuntimeException> implements Supplier<R>{

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

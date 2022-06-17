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
package io.datarouter.util.singletonsupplier;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class SingletonSupplier<R>
extends CheckedSingletonSupplier<R,RuntimeException>
implements Supplier<R>{

	public static <R> SingletonSupplier<R> of(Supplier<? extends R> supplier){
		return new FunctionalSingletonSupplier<>(supplier);
	}

	@Override
	public int hashCode(){
		return Objects.hash(get());
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		@SuppressWarnings("unchecked")
		SingletonSupplier<R> other = (SingletonSupplier<R>)obj;
		return Objects.equals(this.get(), other.get());
	}

	private static class FunctionalSingletonSupplier<R> extends SingletonSupplier<R>{

		private final Supplier<? extends R> supplier;

		public FunctionalSingletonSupplier(Supplier<? extends R> supplier){
			this.supplier = supplier;
		}

		@Override
		protected R load(){
			return supplier.get();
		}

	}

}

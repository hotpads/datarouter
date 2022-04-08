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
package io.datarouter.bytes;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wrapper around a function to handle exceptional values like null.
 */
public class ReplacingFunction<A,B>
implements Function<A,B>{

	private final Function<A,B> function;
	private final A valueToReplace;
	private final Supplier<B> replacementSupplier;

	private ReplacingFunction(
			Function<A,B> function,
			A valueToReplace,
			Supplier<B> replacementSupplier){
		this.function = function;
		this.valueToReplace = valueToReplace;
		this.replacementSupplier = replacementSupplier;
	}

	@Override
	public B apply(A value){
		if(Objects.equals(value, valueToReplace)){
			return replacementSupplier.get();
		}
		return function.apply(value);
	}

	public static <A,B> ReplacingFunction<A,B> of(
			Function<A,B> function,
			A valueToReplace,
			Supplier<B> replacementSupplier){
		return new ReplacingFunction<>(function, valueToReplace, replacementSupplier);
	}

	/**
	 * Replace nulls with something provided by the Supplier
	 */
	public static class NullReplacingFunction<A,B>
	extends ReplacingFunction<A,B>{

		private NullReplacingFunction(
				Function<A,B> function,
				Supplier<B> replacementSupplier){
			super(function, null, replacementSupplier);
		}

		public static <A,B> NullReplacingFunction<A,B> of(
				Function<A,B> function,
				Supplier<B> replacementSupplier){
			return new NullReplacingFunction<>(function, replacementSupplier);
		}

	}

	/**
	 * Replace nulls with nulls.  For wrapping a Function that errors on nulls.
	 */
	public static class NullPassthroughFunction<A,B>
	extends NullReplacingFunction<A,B>{

		private NullPassthroughFunction(Function<A,B> function){
			super(function, () -> null);
		}

		public static <A,B> NullPassthroughFunction<A,B> of(Function<A,B> function){
			return new NullPassthroughFunction<>(function);
		}

	}

}

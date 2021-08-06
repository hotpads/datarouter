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
package io.datarouter.scanner;

import java.util.function.Function;
import java.util.function.Predicate;

import io.datarouter.scanner.ScannerPredicateFunction.ScannerPredicateFunctionResult;

public class ScannerPredicateFunction<T> implements Function<T,ScannerPredicateFunctionResult<T>>{

	private final Predicate<? super T> predicate;

	public ScannerPredicateFunction(Predicate<? super T> predicate){
		this.predicate = predicate;
	}

	@Override
	public ScannerPredicateFunctionResult<T> apply(T item){
		return new ScannerPredicateFunctionResult<>(item, predicate.test(item));
	}

	public static class ScannerPredicateFunctionResult<T>{

		public final T item;
		public final boolean passes;

		public ScannerPredicateFunctionResult(T item, boolean passes){
			this.item = item;
			this.passes = passes;
		}

	}

}

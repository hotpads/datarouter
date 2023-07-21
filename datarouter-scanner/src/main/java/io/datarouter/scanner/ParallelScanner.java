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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ParallelScanner<T>{

	private final Scanner<T> input;
	private final Threads threads;
	private final boolean allowUnorderedResults;

	public ParallelScanner(
			Scanner<T> input,
			Threads threads,
			boolean allowUnorderedResults){
		this.input = input;
		this.threads = threads;
		this.allowUnorderedResults = allowUnorderedResults;
	}

	public <R> Scanner<R> concat(Function<? super T,Scanner<R>> mapper){
		if(threads.useExec()){
			return map(mapper)
					.concat(Function.identity());
		}
		return input.concat(mapper);
	}

	public Scanner<T> each(Consumer<? super T> consumer){
		if(threads.useExec()){
			return map(new ScannerConsumerFunction<>(consumer));
		}
		return input.each(consumer);
	}

	public Scanner<T> exclude(Predicate<? super T> predicate){
		if(threads.useExec()){
			return map(new ScannerPredicateFunction<>(predicate))
					.exclude(result -> result.passes)
					.map(result -> result.item);
		}
		return input.exclude(predicate);
	}

	public void forEach(Consumer<? super T> consumer){
		if(threads.useExec()){
			map(new ScannerConsumerFunction<>(consumer))
					.count();
		}else{
			input.forEach(consumer);
		}
	}

	public Scanner<T> include(Predicate<? super T> predicate){
		if(threads.useExec()){
			return map(new ScannerPredicateFunction<>(predicate))
					.include(result -> result.passes)
					.map(result -> result.item);
		}
		return input.include(predicate);
	}

	public <R> Scanner<R> map(Function<? super T,? extends R> mapper){
		if(threads.useExec()){
			return new ParallelMappingScanner<>(
					input,
					threads,
					allowUnorderedResults,
					mapper);
		}
		return input.map(mapper);
	}

}

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Combine multiple scanners into a scanner of result objects.
 * Items are separated by some extracted key, where a different extract function can be provided for each input.
 * The result may contain zero, one, or multiple items from each input scanner.
 * The caller is responsible for validating the result contents.
 */
public class CombiningScanner<K,R> extends BaseScanner<R>{

	private final Supplier<R> resultSupplier;
	private final Comparator<K> comparator;
	private final List<InputTracker<?,K,R>> inputTrackers;
	private boolean initialized = false;

	public CombiningScanner(
			Supplier<R> resultSupplier,
			Comparator<K> comparator,
			List<InputTracker<?,K,R>> inputTrackers){
		this.resultSupplier = resultSupplier;
		this.comparator = comparator;
		this.inputTrackers = inputTrackers;
	}

	@Override
	public boolean advance(){
		try{
			initializeIfNecessary();
			Optional<K> optNextKey = findNextKey();
			current = optNextKey.map(this::makeResult).orElse(null);
			if(optNextKey.isPresent()){
				return true;
			}else{
				close();
				return false;
			}
		}catch(RuntimeException e){
			close();
			throw e;
		}
	}

	@Override
	public void close(){
		inputTrackers.forEach(InputTracker::closeIfNecessary);
	}

	private void initializeIfNecessary(){
		if(!initialized){
			for(int i = 0; i < inputTrackers.size(); i++){
				InputTracker<?,K,R> input = inputTrackers.get(i);
				input.advanceOrClose();
			}
			initialized = true;
		}
	}

	private Optional<K> findNextKey(){
		return Scanner.of(inputTrackers)
				.include(InputTracker::hasCurrentKey)
				.map(InputTracker::currentKey)
				.findMin(comparator);
	}

	private R makeResult(K activeKey){
		R result = resultSupplier.get();
		for(int i = 0; i < inputTrackers.size(); i++){
			InputTracker<?,K,R> input = inputTrackers.get(i);
			if(input.isClosed){
				continue;
			}
			while(input.hasCurrentKey() && Objects.equals(activeKey, input.currentKey())){
				input.addCurrentToResult(result);
				if(!input.advanceOrClose()){
					break;
				}
			}
		}
		return result;
	}

	/*--------- InputTracker -----------*/

	private static class InputTracker<I,K,R>{
		final Input<I,K,R> input;
		boolean isClosed = false;
		boolean hasCurrentKey = false;
		K currentKey;

		public InputTracker(Input<I,K,R> input){
			this.input = input;
		}

		boolean advanceOrClose(){
			if(isClosed){
				currentKey = null;
				hasCurrentKey = false;
				return false;
			}
			if(input.scanner.advance()){
				currentKey = input.extractor.apply(input.scanner.current());
				hasCurrentKey = true;
				return true;
			}
			currentKey = null;
			hasCurrentKey = false;
			input.scanner.close();
			isClosed = true;
			return false;
		}

		boolean hasCurrentKey(){
			return hasCurrentKey;
		}

		K currentKey(){
			return currentKey;
		}

		void addCurrentToResult(R result){
			input.consumer.accept(result, input.scanner.current());
		}

		void closeIfNecessary(){
			if(!isClosed){
				input.scanner.close();
			}
		}
	}

	/*------------- builder ---------------*/

	public static <K,R> CombiningScannerBuilder<K,R> builder(
			Supplier<R> resultSupplier,
			Comparator<K> comparator){
		return new CombiningScannerBuilder<>(resultSupplier, comparator);
	}

	public static class CombiningScannerBuilder<K,R>{
		private final Supplier<R> resultSupplier;
		private final Comparator<K> comparator;
		private final List<InputTracker<?,K,R>> inputTrackers = new ArrayList<>();

		public CombiningScannerBuilder(Supplier<R> resultSupplier, Comparator<K> comparator){
			this.resultSupplier = resultSupplier;
			this.comparator = comparator;
		}

		public <I> CombiningScannerBuilder<K,R> addInput(
				Scanner<I> scanner,
				Function<I,K> extractor,
				BiConsumer<R,I> consumer){
			Input<I,K,R> input = new Input<>(scanner, extractor, consumer);
			InputTracker<I,K,R> inputWrapper = new InputTracker<>(input);
			inputTrackers.add(inputWrapper);
			return this;
		}

		public CombiningScanner<K,R> build(){
			return new CombiningScanner<>(resultSupplier, comparator, inputTrackers);
		}
	}

	private record Input<I,K,R>(
			Scanner<I> scanner,
			Function<I,K> extractor,
			BiConsumer<R,I> consumer){
	}

}

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

import java.util.Objects;
import java.util.function.Function;

public class SplittingScanner<T,R> extends BaseLinkedScanner<T,Scanner<T>>{

	private final Function<T,R> mapper;
	private boolean outerStarted;
	private boolean foundNext;
	private T nextInnerFirstItem;
	private R currentSplitKey;
	private boolean innerIsActive;

	public SplittingScanner(Scanner<T> input, Function<T,R> mapper){
		super(input);
		this.mapper = mapper;
		this.outerStarted = false;
		this.foundNext = false;
		this.innerIsActive = false;
	}

	@Override
	protected boolean advanceInternal(){
		if(!outerStarted){
			outerStarted = true;
			if(input.advance()){
				foundNext = true;
				nextInnerFirstItem = input.current();
				currentSplitKey = mapper.apply(nextInnerFirstItem);
			}else{
				return false;
			}
		}
		if(innerIsActive){
			while(current.advance()){//in case the user didn't drain the previous inner scanner
			}
		}
		if(foundNext){
			current = new InnerScanner();
			return true;
		}
		return false;
	}

	private class InnerScanner extends BaseScanner<T>{

		private boolean innerStarted;

		private InnerScanner(){
			innerStarted = false;
			innerIsActive = true;
			current = nextInnerFirstItem;
			foundNext = false;
		}

		@Override
		public boolean advance(){
			if(!innerIsActive){
				return false;
			}
			if(!innerStarted){
				innerStarted = true;
				return true;
			}
			if(input.advance()){
				if(Objects.equals(currentSplitKey, mapper.apply(input.current()))){
					current = input.current();
					return true;
				}
				nextInnerFirstItem = input.current();
				currentSplitKey = mapper.apply(nextInnerFirstItem);
				foundNext = true;
			}
			innerIsActive = false;
			return false;
		}

	}

}

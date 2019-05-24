/**
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
package io.datarouter.util.iterable.scanner;

import java.util.ArrayList;

import io.datarouter.util.iterable.scanner.imp.ListBackedScanner;

public class FlatteningScanner<T> implements Scanner<T>{

	private final Scanner<ArrayList<T>> inputScanner;
	private ListBackedScanner<T> currentBatch;
	private boolean finished;

	public FlatteningScanner(Scanner<ArrayList<T>> inputScanner){
		this.inputScanner = inputScanner;
	}

	@Override
	public T getCurrent(){
		return currentBatch.getCurrent();
	}

	@Override
	public boolean advance(){
		if(finished){
			return false;
		}
		if(currentBatch == null){
			return advanceInputScanner();
		}
		return currentBatch.advance() || advanceInputScanner();
	}

	private boolean advanceInputScanner(){
		while(inputScanner.advance()){
			currentBatch = new ListBackedScanner<>(inputScanner.getCurrent());
			if(currentBatch.advance()){
				return true;
			}
		}
		finished = true;
		return false;
	}

}

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
import java.util.Random;

public class ShufflingScanner<T> extends BaseLinkedScanner<T,T>{

	private final Random random;
	private ArrayList<T> items;
	private int index = 0;

	public ShufflingScanner(Scanner<T> input){
		super(input);
		this.random = new Random();
	}

	@Override
	protected boolean advanceInternal(){
		if(items == null){
			items = input.collect(ArrayList::new);
		}
		int numRemaining = items.size() - index;
		if(numRemaining == 0){
			return false;
		}
		int randomLaterIndex = index + random.nextInt(numRemaining);
		current = items.get(randomLaterIndex);
		items.set(randomLaterIndex, items.get(index));
		items.set(index, null);
		++index;
		return true;
	}

}

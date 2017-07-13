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

import java.util.Optional;

//Alternative to Iterator when it is hard to do hasNext()
public interface Scanner<T>{

	T getCurrent();

	/*
	 * Return true if the current value was advanced, otherwise false.  Repeated calls after the initial false should
	 * continue to return false without side effects.
	 */
	boolean advance();

	/**
	 * @return true if the scanner was advanced by the offset
	 */
	default boolean advanceBy(Integer count){
		for(int i = 0; i < Optional.ofNullable(count).orElse(0); i++){
			if(!advance()){
				return false;
			}
		}
		return true;
	}

}

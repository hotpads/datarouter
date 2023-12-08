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
package io.datarouter.bytes.primitivelist;

public class PrimitiveListTool{

	public static void checkIndex(int size, int index){
		if(index < 0){
			String message = String.format("index=%s must be >= 0", index);
			throw new IllegalArgumentException(message);
		}
		if(index >= size){
			String message = String.format("index=%s must be < size=%s", index, size);
			throw new IllegalArgumentException(message);
		}
	}

	public static void checkSubListIndexes(int size, int fromIndex, int toIndex){
		if(fromIndex > toIndex){
			String message = String.format("fromIndex=%s must be <= toIndex=%s", fromIndex, toIndex);
			throw new IllegalArgumentException(message);
		}
		if(fromIndex < 0){
			String message = String.format("fromIndex=%s must be >= 0", fromIndex);
			throw new IllegalArgumentException(message);
		}
		if(toIndex > size){
			String message = String.format("toIndex=%s must be <= size=%s", toIndex, size);
			throw new IllegalArgumentException(message);
		}
	}

}

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
package io.datarouter.util.collection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

public class CollectionTool{

	/*------------------------- null ----------------------------------------*/

	public static <T> Collection<T> nullSafe(Collection<T> in){
		if(in == null){
			return new LinkedList<>();
		}
		return in;
	}

	/*------------------------- empty ---------------------------------------*/

	public static <T> boolean isEmpty(Collection<T> collection){
		if(collection == null || collection.isEmpty()){
			return true;
		}
		return false;
	}

	public static <T> boolean notEmpty(Collection<T> collection){
		if(collection == null || collection.isEmpty()){
			return false;
		}
		return true;
	}

	/*------------------------- size ----------------------------------------*/

	public static boolean differentSize(Collection<?> collectionA, Collection<?> collectionB){
		return sizeNullSafe(collectionA) != sizeNullSafe(collectionB);
	}

	public static int sizeNullSafe(Collection<?> collection){
		if(collection == null){
			return 0;
		}
		return collection.size();
	}

	/*------------------------- first ------------------------------*/

	public static <T> Optional<T> findFirst(Collection<T> collection){
		return Optional.ofNullable(getFirst(collection));
	}

	public static <T> T getFirst(Collection<T> collection){
		if(collection == null || collection.isEmpty()){
			return null;
		}
		return collection.iterator().next();
	}

}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CollectionTool{

	public static <T> boolean nullSafeIsEmpty(Collection<T> collection){
		return collection == null || collection.isEmpty();
	}

	public static <T> boolean nullSafeNotEmpty(Collection<T> collection){
		return collection != null && notEmpty(collection);
	}

	public static <T> boolean notEmpty(Collection<T> collection){
		return !collection.isEmpty();
	}

	public static boolean differentSize(Collection<?> collectionA, Collection<?> collectionB){
		return nullSafeSize(collectionA) != nullSafeSize(collectionB);
	}

	public static int nullSafeSize(Collection<?> collection){
		return collection == null ? 0 : collection.size();
	}

	public static <T> Optional<T> findFirst(Collection<T> collection){
		return Optional.ofNullable(getFirst(collection));
	}

	public static <T> T getFirst(Collection<T> collection){
		if(collection == null || collection.isEmpty()){
			return null;
		}
		return collection.iterator().next();
	}

	public static <T> List<T> shuffleCopy(Collection<T> list){
		List<T> copy = new ArrayList<>(list);
		Collections.shuffle(copy);
		return copy;
	}

}

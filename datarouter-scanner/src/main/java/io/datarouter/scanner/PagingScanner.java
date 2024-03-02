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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Base class for things that page through results by passing the last item as an exclusive start key for the next page
 */
public abstract class PagingScanner<K,T> extends BaseScanner<List<T>>{

	protected final int pageSize;

	protected PagingScanner(int pageSize){
		this.pageSize = pageSize;
	}

	/**
	 * Subclass should transform the last seen item into a key for the next request
	 */
	protected abstract Optional<K> nextParam(T lastSeenItem);

	protected abstract List<T> nextPage(Optional<K> resumeFrom);

	@Override
	public boolean advance(){
		if(current != null && current.size() < pageSize){
			return false;
		}
		T lastSeenItem = getLast(current);
		Optional<K> resumeFrom = nextParam(lastSeenItem);
		current = nextPage(resumeFrom);
		return notEmpty(current);
	}

	private static <T> boolean notEmpty(Collection<T> collection){
		return collection != null && !collection.isEmpty();
	}

	private static <T> T getLast(List<T> list){
		if(notEmpty(list)){
			return list.getLast();
		}
		return null;
	}

}

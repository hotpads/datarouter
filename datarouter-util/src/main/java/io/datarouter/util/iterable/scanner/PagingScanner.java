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

import io.datarouter.util.collection.CollectionTool;

/**
 * Base class for things that page through results by passing the last item as an exclusive start key for the next page
 */
public abstract class PagingScanner<K,T> implements BatchScanner<T>{

	protected final int pageSize;
	private ArrayList<T> current;

	protected PagingScanner(int pageSize){
		this.pageSize = pageSize;
	}

	/**
	 * Subclass should transform the last seen item into a key for the next request
	 */
	protected abstract K extractNextStartKey(T lastSeenItem);

	protected abstract ArrayList<T> nextPage(K resumeFrom);

	@Override
	public ArrayList<T> getCurrent(){
		return current;
	}

	@Override
	public boolean advance(){
		if(current != null && current.size() < pageSize){
			return false;
		}
		T lastSeenItem = CollectionTool.getLast(current);
		K resumeFrom = extractNextStartKey(lastSeenItem);
		current = nextPage(resumeFrom);
		return CollectionTool.notEmpty(current);
	}

}

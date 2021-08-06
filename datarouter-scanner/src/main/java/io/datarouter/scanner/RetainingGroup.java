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

import java.util.LinkedList;

/**
 * Window object returned by {@link RetainingScanner}
 */
public class RetainingGroup<T>{

	private final Object[] retained;

	protected RetainingGroup(LinkedList<T> retainingList){
		retained = new Object[retainingList.size()];
		int idx = 0;
		for(T el : retainingList){
			retained[idx++] = el;
		}
	}

	public T current(){
		return peekBack(0);
	}

	public T previous(){
		return peekBack(1);
	}

	/**
	 * <pre>peekBack(0) == current()
	 *peekBack(n) == nth item before current</pre>
	 * @param peekBackIndex index of previous scanner item
	 */
	@SuppressWarnings("unchecked")
	public T peekBack(int peekBackIndex){
		return (T)retained[peekBackIndex];
	}

}

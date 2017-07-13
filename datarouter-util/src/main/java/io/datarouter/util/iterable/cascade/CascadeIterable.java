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
package io.datarouter.util.iterable.cascade;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * An iterable wrapped around a supplier of iterators.
 * @param <T> the type of the iterated elements
 * @see CascadeIterator
 */
public class CascadeIterable<T> implements Iterable<T>{

	private final Supplier<Iterator<T>> iteratorSupplier;

	public CascadeIterable(final Supplier<Iterator<T>> iteratorSupplier){
		this.iteratorSupplier = iteratorSupplier;
	}

	@Override
	public Iterator<T> iterator(){
		return new CascadeIterator<>(iteratorSupplier);
	}

}

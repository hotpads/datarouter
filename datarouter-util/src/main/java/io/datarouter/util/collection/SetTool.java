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
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SetTool{

	public static <T> Set<T> wrap(T element){
		Set<T> set = new HashSet<>();
		if(element != null){
			set.add(element);
		}
		return set;
	}

	private static <T> SortedSet<T> nullSafeTreeSet(SortedSet<T> in){
		if(in == null){
			return new TreeSet<>();
		}
		return in;
	}

	public static <T> SortedSet<T> nullSafeSortedAddAll(SortedSet<T> set, Collection<T> newItems){
		set = nullSafeTreeSet(set);
		set.addAll(CollectionTool.nullSafe(newItems));
		return set;
	}

	public static class Tests{
		@Test
		public void testNullSafeAddAllWithEmptySet(){
			SortedSet<String> set = new TreeSet<>();
			set.add("b");
			Set<String> toAdd = new HashSet<>();
			toAdd.add("a");
			set = nullSafeSortedAddAll(set, toAdd);
			set.add("c");
			Assert.assertEquals(new String[]{"a", "b", "c"}, set.toArray());
		}

		@Test
		public void testNullSafeAddAllWithNullSet(){
			SortedSet<String> set = null;
			Set<String> toAdd = new HashSet<>();
			toAdd.add("a");
			set = nullSafeSortedAddAll(set, toAdd);
			set.add("c");
			Assert.assertEquals(new String[]{"a", "c"}, set.toArray());
		}
	}

}

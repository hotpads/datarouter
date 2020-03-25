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
package io.datarouter.util.ordered;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.util.Require;
import io.datarouter.util.collection.ListTool;

public class OrderedTool{

	public static <T> List<T> combine(List<Ordered<T>> ordered, List<T> unordered){
		return ListTool.concatenate(sortOrdered(ordered), unordered);
	}

	public static <T> List<T> sortOrdered(List<Ordered<T>> objects){
		List<T> sorted = new ArrayList<>();
		List<Ordered<T>> extraPassObjects = new ArrayList<>();
		for(Ordered<T> object : objects){
			if(object.after == null){
				sorted.add(0, object.item);
				continue;
			}
			int index = sorted.indexOf(object.after);
			if(index != -1){
				sorted.add(index + 1, object.item);
			}else{
				extraPassObjects.add(object);
			}
		}
		// More passes to correct the order - If two or more objects are added out of order and their "after" is not
		// null and their "after: is not yet inserted, they will be out of order.
		long tracker = 0;
		while(!extraPassObjects.isEmpty()){
			for(Ordered<T> object : extraPassObjects){
				// prevent the loop running forever
				if(tracker > extraPassObjects.size()){
					throw new IllegalStateException("The expected order is not possible.");
				}
				int index = sorted.indexOf(object.after);
				if(index != -1){
					sorted.add(index + 1, object.item);
					extraPassObjects.remove(object);
					break;
				}
				tracker++;
			}
		}
		assertOrdered(objects, sorted);
		return sorted;
	}

	private static <T> void assertOrdered(List<Ordered<T>> objects, List<T> sortedObjects){
		Require.isTrue(objects.size() == sortedObjects.size());
		for(Ordered<T> object : objects){
			int indexOfObject = sortedObjects.indexOf(object.item);
			if(object.after == null){
				if(indexOfObject != 0){
					throw new IllegalStateException("There are multiple objects that have a null value for after. "
							+ "There can only be one.");
				}
				continue;
			}
			int indexOfAfter = sortedObjects.indexOf(object.after);
			if(indexOfObject - indexOfAfter != 1){
				throw new IllegalStateException("The expected order is not possible.");
			}
		}
	}

}

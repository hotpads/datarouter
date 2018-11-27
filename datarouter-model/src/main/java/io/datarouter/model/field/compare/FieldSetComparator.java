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
package io.datarouter.model.field.compare;

import java.util.Comparator;
import java.util.Iterator;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;
import io.datarouter.util.lang.ClassTool;

public class FieldSetComparator implements Comparator<FieldSet<?>>{

	@Override
	public int compare(FieldSet<?> left, FieldSet<?> right){
		return compareStatic(left, right);
	}

	public static int compareStatic(FieldSet<?> left, FieldSet<?> right){
		//sort classes alphabetically
		if(right == null){
			return 1;
		}
		if(ClassTool.differentClass(left, right)){
			return left.getClass().getName().compareTo(right.getClass().getName());
		}

		//field by field comparison
		Iterator<Field<?>> thisIterator = left.getFields().iterator();
		Iterator<Field<?>> thatIterator = right.getFields().iterator();
		while(thisIterator.hasNext()){//they will have the same number of fields
			//if we got past the class checks above, then fields should be the same and arrive in the same order
			Field<?> thisField = thisIterator.next();
			Field<?> thatField = thatIterator.next();
			int diff = compareFields(thisField, thatField);
			if(diff != 0){
				return diff;
			}
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public static <T> int compareFields(Field<?> field1, Field<?> field2){
		return ((Field<T>)field1).compareTo((Field<T>)field2);
	}

}

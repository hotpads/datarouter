package com.hotpads.datarouter.storage.field.compare;

import java.util.Comparator;
import java.util.Iterator;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.util.core.lang.ClassTool;

public class FieldSetComparator implements Comparator<FieldSet<?>>{

	@Override
	public int compare(FieldSet<?> fieldA, FieldSet<?> fieldB){
		return compareStatic(fieldA, fieldB);
	}


	public static int compareStatic(FieldSet<?> fieldA, FieldSet<?> fieldB){
		//sort classes alphabetically
		if(fieldB == null){
			return 1;
		}
		if(ClassTool.differentClass(fieldA, fieldB)){
			return fieldA.getClass().getName().compareTo(fieldB.getClass().getName());
		}

		//field by field comparison
		Iterator<Field<?>> thisIterator = fieldA.getFields().iterator();
		Iterator<Field<?>> thatIterator = fieldB.getFields().iterator();
		while(thisIterator.hasNext()){//they will have the same number of fields
			//if we got past the class checks above, then fields should be the same and arrive in the same order
			Field thisField = thisIterator.next();
			Field thatField = thatIterator.next();
			@SuppressWarnings("unchecked")
			int diff = thisField.compareTo(thatField);
			if(diff != 0){
				return diff;
			}
		}
		return 0;
	}
}

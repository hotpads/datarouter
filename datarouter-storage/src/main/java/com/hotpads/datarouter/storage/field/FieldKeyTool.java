package com.hotpads.datarouter.storage.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.util.core.DrIterableTool;

public class FieldKeyTool{

	public static List<String> getNames(Collection<? extends FieldKey<?>> fieldKeys){
		List<String> names = new ArrayList<>();
		for(FieldKey<?> fieldKey : DrIterableTool.nullSafe(fieldKeys)){
			names.add(fieldKey.getName());
		}
		return names;
	}
	
}

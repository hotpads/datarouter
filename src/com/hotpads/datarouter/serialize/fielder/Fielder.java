package com.hotpads.datarouter.serialize.fielder;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;

public interface Fielder<F extends FieldSet<F>>{

	public List<Field<?>> getFields(F fieldSet);
	
}

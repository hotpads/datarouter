package com.hotpads.datarouter.storage.field;

import java.io.Serializable;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.Fielder;

public interface FieldSet<F extends FieldSet<F>>
extends Comparable<FieldSet<F>>,
		Fielder<F>,
		Serializable{ //hibernate composite keys must implement Serializable

	List<Field<?>> getFields();
	List<String> getFieldNames();
	List<?> getFieldValues();
	Object getFieldValue(String fieldName);


	/**************************** serialize ******************/

	@Deprecated //used in jsps.  replace with percent codec
	default String getPersistentString(){
		return FieldSetTool.getPersistentString(getFields());
	}

	@Deprecated //replace with percent codec
	default String getTypedPersistentString(){
		return getClass().getSimpleName()+"_"+getPersistentString();
	}

	@Deprecated //replace with percent codec
	default void fromPersistentString(String in){
		String[] tokens = in.split("_");
		int i = 0;
		for(Field<?> field : getFields()){
			if(i > tokens.length - 1){
				break;
			}
			field.fromString(tokens[i]);
			field.setUsingReflection(this, field.getValue());
			field.setValue(null);// to be safe until Field logic is cleaned up
			++i;
		}
	}

}

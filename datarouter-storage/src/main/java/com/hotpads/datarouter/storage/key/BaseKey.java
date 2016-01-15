package com.hotpads.datarouter.storage.key;

import com.hotpads.datarouter.storage.field.BaseFieldSet;

@SuppressWarnings("serial")
public abstract class BaseKey<K extends Key<K>> 
extends BaseFieldSet<K>
implements Key<K>{

	public static final String NAME = "key";

//	@Override
//	public List<Field<?>> getKeyFields(){
//		return getFields();
//	}
	
//	@Override
//	public void fromPersistentString(String in){
//		String[] tokens = in.split("_");
//		int i=0;
//		for(Field<?> field : this.getFields()){
//			if(i>tokens.length-1){ break; }
//			field.fromString(tokens[i]);
//			field.setUsingReflection(this, field.getValue(), true);
//			field.setValue(null);//to be safe until Field logic is cleaned up
//			++i;
//		}
//	}
	
}

package com.hotpads.datarouter.storage.key;

import java.util.List;

import com.hotpads.datarouter.storage.field.BaseFieldSet;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
public abstract class BaseKey<PK extends PrimaryKey<PK>> 
extends BaseFieldSet
implements Key<PK>{

	public static final String NAME = "key";

	@Override
	public List<Field<?>> getKeyFields(){
		return this.getFields();
	}
	
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

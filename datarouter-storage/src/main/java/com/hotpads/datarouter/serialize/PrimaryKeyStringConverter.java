package com.hotpads.datarouter.serialize;

import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public class PrimaryKeyStringConverter{

	public static <PK extends PrimaryKey<PK>>
	String primaryKeyToString(PK pk, PrimaryKeyFielder<PK> fielder){
		if(pk==null){ return null; }
		return FieldSetTool.getPersistentString(fielder.getFields(pk));
	}

	public static <PK extends PrimaryKey<PK>>
	PK primaryKeyFromString(Class<PK> pkClass, PrimaryKeyFielder<PK> fielder, String s){
		if(s==null){ return null; }
		PK pk = ReflectionTool.create(pkClass);
		String[] tokens = s.split("_");
		int i=0;
		for(Field<?> field : fielder.getFields(pk)){
			if(i>tokens.length-1){ break; }
			field.fromString(tokens[i]);
			field.setUsingReflection(pk, field.getValue());
			field.setValue(null);//to be safe until Field logic is cleaned up
			++i;
		}
		return pk;
	}

}

package com.hotpads.datarouter.serialize.fieldcache;

import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.java.ReflectionTool;

/*
 * try to provide faster reflection by avoiding java.lang.reflect.Field instantiations
 * 
 * even if not much faster, provides a friendlier interface for interacting with reflective fields
 */
public class GenericFieldCache<F extends FieldSet,C extends Class<F>> 
implements FieldCache<F,C>{

	protected C cls;
	protected F sample;//maybe not needed?
	protected List<java.lang.reflect.Field> jFields;
	protected Map<String,java.lang.reflect.Field> jFieldByName;
	
	public GenericFieldCache(C cls){
		this.cls = cls;
		jFields = DrListTool.createArrayList(cls.getDeclaredFields());
		for(java.lang.reflect.Field jField : DrIterableTool.nullSafe(jFields)){
			String jFieldName = jField.getName();
			jFieldByName.put(jFieldName, jField);
		}
		this.sample = ReflectionTool.create(cls);
	}
	
	@Override
	public Object get(F fieldSet, String fieldName){
		try{
			return jFieldByName.get(fieldName).get(fieldSet);
		}catch(IllegalAccessException iae){
			throw new RuntimeException(iae);
		}
	}
	
}

package com.hotpads.datarouter.storage.field;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

public abstract class Field<T extends Comparable<T>>{

	protected String prefix;
	protected String name;
	protected T value;

	
	public Field(String name, T value) {
		this(null, name, value);
	}
	
	public Field(String prefix, String name, T value) {
		this.prefix = prefix;
		this.name = name;
		this.value = value;
	}
	
	public String getPrefixedName(){
		if(StringTool.isEmpty(prefix)){
			return name;
		}else{
			return prefix + "." + name;
		}
	}
	
	@Override
	public String toString() {
		return this.getPrefixedName()+":"+this.getValue();
	}
	
	public abstract String getSqlEscaped();
	
	public String getSqlNameValuePairEscaped(){
		if(value==null){
			return this.name+" is null";
		}
		return this.name+"="+this.getSqlEscaped();
	}
	
	public abstract T parseJdbcValueButDoNotSet(Object col);
	
	public void setFieldUsingBeanUtils(FieldSet targetFieldSet, Object col){
		try{
			T value = this.parseJdbcValueButDoNotSet(col);
			PropertyUtils.setProperty(targetFieldSet, this.getName(), value);
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()
					+" on "+targetFieldSet.getClass().getSimpleName()+"."+this.getName());
		}
	}
	
	public void setFieldUsingReflection(FieldSet targetFieldSet, Object col){
		try{
			T value = this.parseJdbcValueButDoNotSet(col);
			//method Field.getDeclaredField(String) allows access to non-public fields
			java.lang.reflect.Field fld = targetFieldSet.getClass().getDeclaredField(this.getName());
			fld.setAccessible(true);
			fld.set(targetFieldSet, value);
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()
					+" on "+targetFieldSet.getClass().getSimpleName()+"."+this.getName());
		}
	}
	
	public static List<Field<?>> createList(Field<?>... fields){
		return ListTool.createArrayList(fields);
	}
	
	public static int countNonNullLeadingFields(Iterable<Field<?>> fields){
		int count = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(field.getValue() != null){
				++count;
			}else{
				break;
			}
		}
		return count;
	}
	
	/********************************** get/set ******************************************/

	
	public void setPrefix(String prefix){
		this.prefix = prefix;
	}
	
	public String getPrefix(){
		return prefix;
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setValue(T value){
		this.value = value;
	}
	
	
}

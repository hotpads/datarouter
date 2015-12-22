package com.hotpads.datarouter.storage.field;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseField<T> 
implements Field<T>{

	private String prefix;//ignore if not needed
	private java.lang.reflect.Field jField;//ignore in subclasses if more complex structure needed
	protected T value;
	
	/*************************** constructor *********************************/
	public BaseField(String prefix, T value) {		
		this.prefix = DrStringTool.nullSafe(prefix);
		this.value = value;
	}
	
	/******************************** methods *******************************/
	
	@Override
	public String toString() {
		return getPrefixedName()+":"+getValueString();
	}
	
	public synchronized void cacheReflectionInfo(Object sampleFieldSet){
		List<String> fieldNames = new LinkedList<>();
		if(DrStringTool.notEmpty(getPrefix())){
			fieldNames = DrListTool.createArrayList(getPrefix().split("\\."));
		}
		fieldNames.add(getKey().getName());
		try{
			jField = ReflectionTool.getNestedField(sampleFieldSet, fieldNames);
			jField.setAccessible(true);//redundant
		}catch(IllegalArgumentException e){
			throw new RuntimeException(e);
		}
	}
	
	
	@Override
	public void fromString(String s){
		this.value = parseStringEncodedValueButDoNotSet(s);
	}
	
	
	/****************************** ByteField ***********************************/
	
	@Override
	public byte[] getBytesWithSeparator(){
		return getBytes();
	}
	
	@Override
	public T fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		return fromBytesButDoNotSet(bytes, byteOffset);
	}
	
	
	/******************************* reflective setters *******************************/

	@Override
	public void setUsingReflection(Object targetFieldSet, Object pValue){
		try{
			Object nestedFieldSet = FieldTool.getNestedFieldSet(targetFieldSet, this);
			if(jField==null){ 
				cacheReflectionInfo(targetFieldSet); 
			}
			jField.set(nestedFieldSet, pValue);
		}catch(Exception e){
			String message = e.getClass().getSimpleName()
					+" on "+targetFieldSet.getClass().getSimpleName()+"."+getKey().getName();
			throw new DataAccessException(message, e);
		}
	}


	@Override
	public String getPrefixedName(){
		if(DrStringTool.isEmpty(prefix)){
			return getKey().getName();
		}
		return prefix + "." + getKey().getName();
	}
	
	@Override
	public String getPrefix(){
		return prefix;
	}
	
	@Override
	public Field<T> setPrefix(String prefix){
		this.prefix = prefix;
		return this;
	}

	@Override
	public Field<T> setValue(T value){
		this.value = value;
		return this;
	}

	@Override
	public T getValue() {
		return value;
	}

	
	public static class FieldColumnNameComparator implements Comparator<Field<?>>{
		@Override
		public int compare(Field<?> o1, Field<?> o2){
			return o1.getKey().getColumnName().hashCode() - o2.getKey().getColumnName().hashCode();
		}
	}
	
}





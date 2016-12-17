package com.hotpads.datarouter.storage.field;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseField<T> implements Field<T>{

	private static final Map<String, java.lang.reflect.Field> columnNameToFieldMap = new ConcurrentHashMap<>();

	private String prefix;//ignore if not needed
	protected T value;

	/*************************** constructor *********************************/

	public BaseField(String prefix, T value){
		this.prefix = DrStringTool.nullSafe(prefix);
		this.value = value;
	}

	/******************************** methods *******************************/

	@Override
	public int getValueHashCode(){
		return value == null ? 0 : value.hashCode();
	}

	@Override
	public String toString(){
		return getPrefixedName() + ":" + getValueString();
	}

	@Override
	public void fromString(String valueAsString){
		this.value = parseStringEncodedValueButDoNotSet(valueAsString);
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
	public void setUsingReflection(Object targetFieldSet, Object fieldValue){
		try{
			Object nestedFieldSet = FieldTool.getNestedFieldSet(targetFieldSet, this);
			String cacheKey = getFieldCacheKey(targetFieldSet, nestedFieldSet);
			java.lang.reflect.Field javaField = columnNameToFieldMap.get(cacheKey);
			if(javaField == null){
				javaField = ReflectionTool.getDeclaredFieldFromHierarchy(nestedFieldSet.getClass(), getKey().getName());
				columnNameToFieldMap.put(cacheKey, javaField);
			}
			javaField.set(nestedFieldSet, fieldValue);
		}catch(Exception e){
			String message = e.getClass().getSimpleName()
					+ " on " + targetFieldSet.getClass().getSimpleName() + "." + getKey().getName();
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
	public T getValue(){
		return value;
	}

	@Override
	public String getPreparedStatementValue(){
		return "?";
	}

	public static class FieldColumnNameComparator implements Comparator<Field<?>>{
		@Override
		public int compare(Field<?> o1, Field<?> o2){
			return o1.getKey().getColumnName().hashCode() - o2.getKey().getColumnName().hashCode();
		}
	}

	private String getFieldCacheKey(Object targetFieldSet, Object nestedFieldSet){
		String cacheKey = targetFieldSet.getClass().getName();
		if(DrStringTool.notEmpty(prefix)){
			cacheKey += nestedFieldSet.getClass().getName();
		}
		return cacheKey + getPrefixedName();
	}
}
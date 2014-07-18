package com.hotpads.datarouter.storage.field;

import java.sql.ResultSet;
import java.util.Comparator;
import java.util.List;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseField<T> implements Field<T>{

	protected java.lang.reflect.Field jField;//ignore in subclasses if more complex structure needed
	protected String prefix;//ignore if not needed
	protected String name;
	protected String columnName;//defaults to name if not specified
	protected T value;//TODO move this out of the field descriptor
	protected Boolean nullable = true;

	
	/*************************** constructor *********************************/
	
	public BaseField(String name, T value) {
		this(null, name, value);
	}
	
	public BaseField(String prefix, String name, T value) {
		this.prefix = prefix;
		this.name = name;
		this.columnName = name;
		this.value = value;
	}
	
	
	/******************************** methods *******************************/
	
	@Override
	public String toString() {
		return getPrefixedName()+":"+getValueString();
	}
	
	@Override
	public String getPrefixedName(){
		if(StringTool.isEmpty(prefix)){
			return name;
		}else{
			return prefix + "." + name;
		}
	}
	
//	public void setPrefixAndName(String prefixAndName){
//		String[] tokens = prefixAndName.split(".");
//		if(tokens.length > 1){
//			setPrefix(tokens[0]);
//			setName(tokens[1]);
//		}else{
//			setName(tokens[0]);
//		}
//	}
	
	@Override
	public boolean isCollection(){
		return false;
	}
	
	@Override
	public void cacheReflectionInfo(Object sampleFieldSet){
//		ObjectTool.checkNotNull(sampleFieldSet);
//		if(StringTool.notEmpty(prefix)){
//			java.lang.reflect.Field prefixField = ReflectionTool.getDeclaredFieldFromHierarchy(
//					sampleFieldSet.getClass(), prefix);
//			prefixField.setAccessible(true);
//			if(ReflectionTool.get(prefixField, sampleFieldSet)==null){
//				ReflectionTool.set(prefixField, sampleFieldSet, ReflectionTool.create(prefixField.getType()));
//			}
//			jField = ReflectionTool.getDeclaredFieldFromHierarchy(
//					prefixField.getType(), name);
//		}else{
//			jField = ReflectionTool.getDeclaredFieldFromHierarchy(sampleFieldSet.getClass(), name);
//		}
//		jField.setAccessible(true);
		List<String> fieldNames = ListTool.createLinkedList();
		if(StringTool.notEmpty(prefix)){
			fieldNames = ListTool.createArrayList(prefix.split("\\."));
		}
		fieldNames.add(name);
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
	public byte[] getColumnNameBytes(){
		return StringByteTool.getUtf8Bytes(columnName);
	}
	
//	@Override
//	public byte[] getBytes(){
//		throw new NotImplementedException("still waiting on float and double serialization");	
//	};
	
	@Override
	public boolean isFixedLength(){
		return true;//override if false
	}
	
	@Override
	public byte[] getBytesWithSeparator(){
		return getBytes();
	}
	
	@Override
	public T fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		return fromBytesButDoNotSet(bytes, byteOffset);
	}
	
	/**************************** SqlField ****************************************/
	
	@Override
	public String getSqlNameValuePairEscaped(){
		if(value==null){
			return columnName+" is null";
		}
		return columnName+"="+this.getSqlEscaped();
	}
	
	
	/******************************* reflective setters *******************************/

	@Override
	public void fromHibernateResultUsingReflection(Object targetFieldSet, Object col){
		T v = parseJdbcValueButDoNotSet(col);
		setUsingReflection(targetFieldSet, v);
	}
	
	@Override
	public void fromJdbcResultSetUsingReflection(Object targetFieldSet, ResultSet resultSet){
		T v = fromJdbcResultSetButDoNotSet(resultSet);
		setUsingReflection(targetFieldSet, v);
	}

//	@Override
//	public void setUsingReflection(FieldSet<?> targetFieldSet, Object value, boolean ignorePrefix){
//		try{
//			//method Field.getDeclaredField(String) allows access to non-public fields
//			if( ! ignorePrefix && this.getPrefix()!=null){
//				java.lang.reflect.Field parentField = ReflectionTool.getDeclaredFieldFromHierarchy(
//						targetFieldSet.getClass(), getPrefix());
//				parentField.setAccessible(true);
//				if(parentField.get(targetFieldSet)==null){
//					parentField.set(targetFieldSet, ReflectionTool.create(parentField.getType()));
//				}
//				java.lang.reflect.Field childField = ReflectionTool.getDeclaredFieldFromHierarchy(
//						parentField.getType(), getName());
//				childField.setAccessible(true);
//				childField.set(parentField.get(targetFieldSet), value);
//			}else{
//				java.lang.reflect.Field fld = ReflectionTool.getDeclaredFieldFromHierarchy(
//						targetFieldSet.getClass(), getName());
//				fld.setAccessible(true);
//				fld.set(targetFieldSet, value);
//			}
//		}catch(Exception e){
//			throw new DataAccessException(e.getClass().getSimpleName()
//					+" on "+targetFieldSet.getClass().getSimpleName()+"."+getName());
//		}
//	}
	
	@Override
	public void setUsingReflection(Object targetFieldSet, Object pValue){
		try{
//			//method Field.getDeclaredField(String) allows access to non-public fields
//			if( ! ignorePrefix && this.getPrefix()!=null){
//				java.lang.reflect.Field parentField = ReflectionTool.getDeclaredFieldFromHierarchy(
//						targetFieldSet.getClass(), getPrefix());
//				parentField.setAccessible(true);
//				if(parentField.get(targetFieldSet)==null){
//					parentField.set(targetFieldSet, ReflectionTool.create(parentField.getType()));
//				}
//				java.lang.reflect.Field childField = ReflectionTool.getDeclaredFieldFromHierarchy(
//						parentField.getType(), getName());
//				childField.setAccessible(true);
//				childField.set(parentField.get(targetFieldSet), value);
//			}else{
//				java.lang.reflect.Field fld = ReflectionTool.getDeclaredFieldFromHierarchy(
//						targetFieldSet.getClass(), getName());
//				fld.setAccessible(true);
//				fld.set(targetFieldSet, value);
//			}
			
			
//			FieldSet<?> nestedFieldSet = (FieldSet<?>)FieldTool.getNestedFieldSet(targetFieldSet, this);//bad code - does not necessarily return a FieldSet
			Object nestedFieldSet = FieldTool.getNestedFieldSet(targetFieldSet, this);
//			java.lang.reflect.Field fld = FieldTool.getReflectionFieldForField(nestedFieldSet, this);
//			fld.set(nestedFieldSet, value);
			if(jField==null){ cacheReflectionInfo(targetFieldSet); }//for fromPersistentString (ViewNodeDataController)
			jField.set(nestedFieldSet, pValue);
		}catch(Exception e){
			String message = e.getClass().getSimpleName()
					+" on "+targetFieldSet.getClass().getSimpleName()+"."+getName();
			throw new DataAccessException(message, e);
		}
	}

	
	/********************************** get/set ******************************************/

	@Override
	public Field<T> setPrefix(String prefix){
		this.prefix = prefix;
		return this;
	}

	@Override
	public String getPrefix(){
		return prefix;
	}

	@Override
	public Field<T> setName(String name){
		this.name = name;
		return this;
	}

	@Override
	public String getName() {
		return name;
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

	@Override
	public Field<T> setColumnName(String columnName){
		this.columnName = columnName;
		return this;
	}

	@Override
	public String getColumnName(){
		return columnName;
	}
	
	public Boolean getNullable() {
		return nullable;
	}
	
	//TODO would be nice to return the subclass
	public BaseField<T> setNullable(Boolean b) {
		this.nullable = b;
		return this;
	}

	public static class FieldColumnNameComparator implements Comparator<Field<?>>{
		@Override
		public int compare(Field<?> o1, Field<?> o2){
			return o1.getColumnName().hashCode() - o2.getColumnName().hashCode();
		}
	}
	
}





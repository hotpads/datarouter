package com.hotpads.datarouter.storage.field;

import java.sql.ResultSet;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.datarouter.util.core.StringTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseField<T> implements Field<T>{
	private static final Logger logger = LoggerFactory.getLogger(BaseField.class);
	
	protected java.lang.reflect.Field jField;//ignore in subclasses if more complex structure needed
	protected String prefix;//ignore if not needed
	protected String name;//the name of the java field
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
	
	@Override
	public boolean isCollection(){
		return false;
	}
	
	@Override
	public synchronized void cacheReflectionInfo(Object sampleFieldSet){
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





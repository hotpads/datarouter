package com.hotpads.datarouter.storage.field;

import java.sql.ResultSet;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseField<T> implements Field<T>{

	protected String prefix;
	protected String name;
	protected T value;

	
	/*************************** constructor *********************************/
	
	public BaseField(String name, T value) {
		this(null, name, value);
	}
	
	public BaseField(String prefix, String name, T value) {
		this.prefix = prefix;
		this.name = name;
		this.value = value;
	}
	
	
	/******************************** methods *******************************/
	
	@Override
	public String toString() {
		return this.getPrefixedName()+":"+this.getValue();
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
	
	
	/****************************** ByteField ***********************************/
	
	public byte[] getMicroNameBytes(){
		return StringByteTool.getUtf8Bytes(this.getName());//TODO get micro name
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
			return this.name+" is null";
		}
		return this.name+"="+this.getSqlEscaped();
	}
	
	
	/******************************* reflective setters *******************************/

	@Override
	public void fromHibernateResultUsingReflection(FieldSet targetFieldSet, Object col, boolean ignorePrefix){
		T v = this.parseJdbcValueButDoNotSet(col);
		this.setUsingReflection(targetFieldSet, v, ignorePrefix);
	}

	@Override
	public void fromJdbcResultSetUsingReflection(FieldSet targetFieldSet, ResultSet resultSet, boolean ignorePrefix){
		T v = this.fromJdbcResultSetButDoNotSet(resultSet);
		this.setUsingReflection(targetFieldSet, v, ignorePrefix);
	}

	@Override
	public void setUsingReflection(FieldSet targetFieldSet, Object value, boolean ignorePrefix){
		try{
			//method Field.getDeclaredField(String) allows access to non-public fields
			if( ! ignorePrefix && this.getPrefix()!=null){
				java.lang.reflect.Field parentField = ReflectionTool.getDeclaredFieldFromHierarchy(
						targetFieldSet.getClass(), this.getPrefix());
				parentField.setAccessible(true);
				if(parentField.get(targetFieldSet)==null){
					parentField.set(targetFieldSet, ReflectionTool.create(parentField.getType()));
				}
				java.lang.reflect.Field childField = ReflectionTool.getDeclaredFieldFromHierarchy(
						parentField.getType(), this.getName());
				childField.setAccessible(true);
				childField.set(parentField.get(targetFieldSet), value);
			}else{
				java.lang.reflect.Field fld = ReflectionTool.getDeclaredFieldFromHierarchy(
						targetFieldSet.getClass(), this.getName());
				fld.setAccessible(true);
				fld.set(targetFieldSet, value);
			}
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()
					+" on "+targetFieldSet.getClass().getSimpleName()+"."+this.getName());
		}
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





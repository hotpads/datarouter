package com.hotpads.datarouter.storage.field;

import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.NotImplementedException;

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
	
	
	/****************************** ByteField ***********************************/
	
	@Deprecated
	public byte[] getBytes(){
		throw new NotImplementedException("still waiting on float and double serialization");	
	};
	
	
	/**************************** SqlField ****************************************/
	
	@Override
	public String getSqlNameValuePairEscaped(){
		if(value==null){
			return this.name+" is null";
		}
		return this.name+"="+this.getSqlEscaped();
	}

	@Override
	public void setFieldUsingBeanUtils(FieldSet targetFieldSet, Object col){
		try{
			T value = this.parseJdbcValueButDoNotSet(col);
			PropertyUtils.setProperty(targetFieldSet, this.getName(), value);
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()
					+" on "+targetFieldSet.getClass().getSimpleName()+"."+this.getName());
		}
	}

	@Override
	public void fromJdbcResultSetUsingReflection(FieldSet targetFieldSet, ResultSet resultSet){
		try{
			T value = this.fromJdbcResultSetButDoNotSet(resultSet);
			//method Field.getDeclaredField(String) allows access to non-public fields
			if(this.getPrefix()!=null){
				java.lang.reflect.Field parentField = targetFieldSet.getClass().getDeclaredField(this.getPrefix());
				parentField.setAccessible(true);
				java.lang.reflect.Field childField = parentField.getType().getDeclaredField(this.getName());
				childField.setAccessible(true);
				childField.set(parentField.get(targetFieldSet), value);
			}else{
				java.lang.reflect.Field fld = targetFieldSet.getClass().getDeclaredField(this.getName());
				fld.setAccessible(true);
				fld.set(targetFieldSet, value);
			}
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()
					+" on "+targetFieldSet.getClass().getSimpleName()+"."+this.getName());
		}
	}

	@Override
	public void setFieldUsingReflection(FieldSet targetFieldSet, Object col){
		try{
			T value = this.parseJdbcValueButDoNotSet(col);
			//method Field.getDeclaredField(String) allows access to non-public fields
			if(this.getPrefix()!=null){
				java.lang.reflect.Field parentField = targetFieldSet.getClass().getDeclaredField(this.getPrefix());
				parentField.setAccessible(true);
				java.lang.reflect.Field childField = parentField.getClass().getDeclaredField(this.getName());
				childField.setAccessible(true);
				childField.set(parentField.get(targetFieldSet), value);
			}else{
				java.lang.reflect.Field fld = targetFieldSet.getClass().getDeclaredField(this.getName());
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





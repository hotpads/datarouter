package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.util.core.bytes.StringByteTool;

public abstract class BaseFieldKey<T> 
implements FieldKey<T>{
	
	private final String name;//the name of the java field
	private final String columnName;//defaults to name if not specified
	private final boolean nullable;
	private final FieldGeneratorType autoGeneratedType;
	private final T defaultValue;
	
	/*************************** constructor *********************************/
	
	public BaseFieldKey(String name){
		this(name, true, FieldGeneratorType.NONE);
	}
	
	public BaseFieldKey(String name, T defaultValue){
		this(name, name, true, FieldGeneratorType.NONE, defaultValue);
	}
	
	//use java field name for columnName
	public BaseFieldKey(String name, boolean nullable, FieldGeneratorType fieldGeneratorType){
		this(name, name, nullable, fieldGeneratorType, null);
	}
	
	public BaseFieldKey(String name, String columnName, boolean nullable, 
			FieldGeneratorType fieldGeneratorType){
		this(name, columnName, nullable, fieldGeneratorType, null);		
	}
	
	public BaseFieldKey(String name, String columnName, boolean nullable, 
			FieldGeneratorType fieldGeneratorType, T defaultValue){
		this.name = name;
		this.columnName = columnName;
		this.nullable = nullable;
		this.autoGeneratedType = fieldGeneratorType;
		this.defaultValue = defaultValue;
	}
	
	/******************************** methods *******************************/
	
	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + ":" + name + "]";
	}
	
	@Override
	public boolean isFixedLength(){
		return true;
	}
	
	@Override
	public boolean isCollection(){
		return false;
	}
	
	//don't cache this until we are using keys where it would be allocated on every equals/hashCode/compareTo
	@Override
	public byte[] getColumnNameBytes(){
		return StringByteTool.getUtf8Bytes(columnName);
	}
	
	@Override
	public T getDefaultValue(){
		return defaultValue;
	}
	
	/********************************** get/set ******************************************/

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getColumnName(){
		return columnName;
	}
	
	@Override
	public boolean isNullable() {
		return nullable;
	}
	
	@Override
	public FieldGeneratorType getAutoGeneratedType(){
		return autoGeneratedType;
	}	

}




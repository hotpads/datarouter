package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.util.core.DrComparableTool;

public abstract class BasePrimitiveField<T extends Comparable<T>> 
extends BaseField<T>{

	private final PrimitiveFieldKey<T> key;	
	
	public BasePrimitiveField(PrimitiveFieldKey<T> key, T value){
		super(null, value);
		this.key = key;
	}
	
	public BasePrimitiveField(String prefix, PrimitiveFieldKey<T> key, T value){
		super(prefix, value);
		this.key = key;
	}

	public BasePrimitiveField(String name, T value){
		this(null, name, value);
	}
	
	public BasePrimitiveField(String name, T value, T defaultValue){
		this(null, name, value, defaultValue);
	}
	
	public BasePrimitiveField(String prefix, String name, T value){
		this(prefix, name, value, null);		
	}
	
	public BasePrimitiveField(String prefix, String name, T value, T defaultValue){		
		super(prefix, value);
		this.key = new PrimitiveFieldKey<>(name, defaultValue);		
	}
	public BasePrimitiveField(String prefix, String name, boolean nullable, FieldGeneratorType fieldGeneratorType,
			T value){
		super(prefix, value);
		this.key = new PrimitiveFieldKey<>(name, nullable, fieldGeneratorType);
	}
	
	public BasePrimitiveField(String prefix, String name, String columnName, boolean nullable,
			FieldGeneratorType fieldGeneratorType, T value){
		super(prefix, value);
		this.key = new PrimitiveFieldKey<>(name, columnName, nullable, fieldGeneratorType);
	}
	
	
	@Override
	public FieldKey<T> getKey(){
		return key;
	}
	
	@Override
	public int compareTo(Field<T> other){
		if(other==null){ return 1; }
		return DrComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	}
	
	@Override
	public String getValueString(){
		if(value==null){ return ""; }
		return value.toString();
	}

	
	
}

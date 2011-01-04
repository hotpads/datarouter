package com.hotpads.datarouter.storage.field;

import java.util.Iterator;
import java.util.List;

import com.hotpads.datarouter.serialize.JsonTool;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.CollectionTool;

@SuppressWarnings("serial")
public abstract class BaseFieldSet implements FieldSet{
	
	
	/******** standard *************************/
	
	@Override
	public boolean equals(Object that){
		if(that==null){ return false; }
		if( ! (getClass().equals(that.getClass()))){ return false; }
		return 0 == compareTo((FieldSet)that);
	}
	
	@Override
	public int hashCode(){
		//preserve order
//		int hash = 0;
//        for (Comparable<?> fieldValue : CollectionTool.nullSafe(this.getFieldValues())){
//        	hash = 31*hash + fieldValue.hashCode();
//        }
//        return hash;
		
		//but order doesn't matter that much for us
		int hash = 0;
		for (Object fieldValue : CollectionTool.nullSafe(getFieldValues())){
			if(fieldValue != null){
				hash = hash ^ fieldValue.hashCode();
			}
		}
	  return hash;
		
	}
	
	/*
	 * WARNING - FieldSets are compared based only on their key fields.  Content is not compared by default
	 */
	@Override
	public int compareTo(FieldSet that){
		//sort classes alphabetically
		if(that==null){ return 1; }
		if(ClassTool.differentClass(this, that)){
			return getClass().getName().compareTo(that.getClass().getName());
		}
		
		//field by field comparison
		Iterator<Field<?>> thisIterator = this.getKeyFields().iterator();
		Iterator<Field<?>> thatIterator = that.getKeyFields().iterator();
		while(thisIterator.hasNext()){//they will have the same number of fields
			//if we got past the class checks above, then fields should be the same and arrive in the same order
			@SuppressWarnings("unchecked")
			Field thisField = thisIterator.next();
			@SuppressWarnings("unchecked")
			Field thatField = thatIterator.next();
			@SuppressWarnings("unchecked")
			int diff = thisField.compareTo(thatField);
			if(diff != 0){ return diff; }
		}
		return 0;
	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName()+"."+this.getPersistentString();
	}
	
	/**************************** serialize ******************/

	@Override
	public String getPersistentString(){
		StringBuilder sb = new StringBuilder();
		boolean doneOne = false;
		for(Field<?> field : getFields()){
			if(doneOne){ 
				sb.append("_");
			}
			sb.append(field.getValue());
			doneOne = true;
		}
		return sb.toString();
	}
	
	@Override
	public String getTypedPersistentString(){
		return this.getClass().getSimpleName()+"_"+getPersistentString();
	}	
	
	@Override
	public void fromPersistentString(String in){
		String[] tokens = in.split("_");
		int i=0;
		for(Field<?> field : getFields()){
			if(i>tokens.length-1){ break; }
			field.fromString(tokens[i]);
			field.setUsingReflection(this, field.getValue(), true);
			field.setValue(null);//to be safe until Field logic is cleaned up
			++i;
		}
	}
	
	/****************** fields ************************/

	@Override
	public List<String> getFieldNames(){
		return FieldTool.getFieldNames(getFields());
	}

	@Override
	public List<?> getFieldValues(){
		return FieldTool.getFieldValues(getFields());
	}

	@Override
	public Object getFieldValue(String fieldName){
		return FieldTool.getFieldValue(getFields(), fieldName);
	}
	
	/**************************** json ******************/
	
	@Override
	public String getJson(){
		return JsonTool.getJson(getFields()).toString();
	}
	
	/**************************** sql ******************/

	@Override
	public List<String> getSqlValuesEscaped(){
		return FieldTool.getSqlValuesEscaped(getFields());
	}

	@Override
	public List<String> getSqlNameValuePairsEscaped(){
		return FieldTool.getSqlNameValuePairsEscaped(getFields());
	}

	@Override
	public String getSqlNameValuePairsEscapedConjunction(){
		return FieldTool.getSqlNameValuePairsEscapedConjunction(getFields());
	}
	
	/**************************** bytes ******************/
	
	@Override
	public byte[] getBytes(boolean allowNulls){
		try{
			return FieldSetTool.getConcatenatedValueBytes(getFields(), allowNulls);
		}catch(Exception e){
			throw new IllegalArgumentException("error on getBytes(allowNulls="+allowNulls+") for "+this.toString());
		}
	}
	
	
}













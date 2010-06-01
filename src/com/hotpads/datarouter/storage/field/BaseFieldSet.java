package com.hotpads.datarouter.storage.field;

import java.util.Iterator;
import java.util.List;

import com.hotpads.datarouter.serialize.JsonTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;

@SuppressWarnings("serial")
public abstract class BaseFieldSet implements FieldSet{
	
	
	/******** comparable *************************/
	
	@Override
	public boolean equals(Object that){
		if(that==null){ return false; }
		if( ! (this.getClass().equals(that.getClass()))){ return false; }
		return 0 == this.compareTo((FieldSet)that);
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
		for (Comparable<?> fieldValue : CollectionTool.nullSafe(this.getFieldValues())){
			if(fieldValue != null){
				hash = hash ^ fieldValue.hashCode();
			}
		}
	  return hash;
		
	}
	
	@Override
	public int compareTo(FieldSet that){
		//sort classes alphabetically
		if(that==null){ return 1; }
		int classDiff = this.getClass().getName().compareTo(that.getClass().getName());
		if(classDiff != 0){ return classDiff; }
		
		//field by field comparison
		Iterator<Comparable<?>> thisIterator = this.getFieldValues().iterator();
		Iterator<Comparable<?>> thatIterator = that.getFieldValues().iterator();
		while(thisIterator.hasNext()){
			Comparable<?> thisFieldVal = thisIterator.next();
			Comparable<?> thatFieldVal = thatIterator.next();
			int diff = ComparableTool.nullFirstCompareTo(thisFieldVal, thatFieldVal);
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
		for(Field<?> field : this.getFields()){
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
		return this.getClass().getSimpleName()+"_"+this.getPersistentString();
	}	
	
	/****************** fields ************************/

	@Override
	public List<String> getFieldNames(){
		return FieldTool.getFieldNames(this.getFields());
	}

	@Override
	public List<Comparable<?>> getFieldValues(){
		return FieldTool.getFieldValues(this.getFields());
	}

	@Override
	public Comparable<?> getFieldValue(String fieldName){
		return FieldTool.getFieldValue(this.getFields(), fieldName);
	}
	
	/**************************** json ******************/
	
	@Override
	public String getJson(){
		return JsonTool.getJson(this.getFields()).toString();
	}
	
	/**************************** sql ******************/

	@Override
	public List<String> getSqlValuesEscaped(){
		return FieldTool.getSqlValuesEscaped(this.getFields());
	}

	@Override
	public List<String> getSqlNameValuePairsEscaped(){
		return FieldTool.getSqlNameValuePairsEscaped(this.getFields());
	}

	@Override
	public String getSqlNameValuePairsEscapedConjunction(){
		return FieldTool.getSqlNameValuePairsEscapedConjunction(this.getFields());
	}
}

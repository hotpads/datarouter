package com.hotpads.datarouter.storage.key;

import java.util.Iterator;
import java.util.List;

import com.hotpads.datarouter.serialize.JsonTool;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;

@SuppressWarnings("serial")
public abstract class BaseKey<D extends Databean> 
implements Key<D>{  //hibernate composite keys must implement serializable
	
	public static final String DEFAULT_KEY_NAME = "key";
		
	protected Class<D> databeanClass;
	
	protected BaseKey(Class<D> databeanClass) {
		super();
		this.databeanClass = databeanClass;
	}

	@Override
	public Class<D> getDatabeanClass() {
		return this.databeanClass;
	}

	@Override
	public String getDatabeanName() {
		return this.databeanClass.getSimpleName();
	}

	@Override
	public String getPhysicalNodeName() {
		return this.getDatabeanName();
	}
	
	/******** comparable *************************/
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object that){
		if(that==null){ return false; }
		if( ! (this.getClass().equals(that.getClass()))){ return false; }
		return 0 == this.compareTo((Key<D>)that);
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
	public int compareTo(Key<D> that){
		//sort classes alphabetically
		if(that==null){ return 1; }
		int classDiff = this.getDatabeanClass().getName().compareTo(that.getDatabeanClass().getName());
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
		return this.getDatabeanName()+"_"+this.getPersistentString();
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

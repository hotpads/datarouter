package com.hotpads.datarouter.storage.key;

import java.util.Iterator;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.ListTool;

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
	
	
	/****************** fields ************************/

	@Override
	public List<String> getFieldNames(){
		List<String> fieldNames = ListTool.createLinkedList();
		for(Field field : this.getFields()){
			fieldNames.add(field.getName());
		}
		return fieldNames;
	}

	@Override
	public List<Comparable<?>> getFieldValues(){
		List<Comparable<?>> fieldValues = ListTool.createLinkedList();
		for(Field field : this.getFields()){
			fieldValues.add(field.getValue());
		}
		return fieldValues;
	}

	@Override
	public Comparable<?> getFieldValue(String fieldName){
		for(Field field : this.getFields()){
			if(field.getName().equals(fieldName)){
				return field.getValue();
			}
		}
		return null;
	}

	@Override
	public String getPersistentString(){
		StringBuilder sb = new StringBuilder();
		boolean doneOne = false;
		for(Field field : this.getFields()){
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

	@Override
	public List<String> getSqlValuesEscaped(){
		List<String> sql = ListTool.createLinkedList();
		for(Field field : this.getFields()){
			sql.add(field.getSqlEscaped());
		}
		return sql;
	}

	@Override
	public List<String> getSqlNameValuePairsEscaped(){
		List<String> sql = ListTool.createLinkedList();
		for(Field field : this.getFields()){
			sql.add(field.getSqlNameValuePairEscaped());
		}
		return sql;
	}

	@Override
	public String getSqlNameValuePairsEscapedConjunction(){
		List<String> nameValuePairs = getSqlNameValuePairsEscaped();
		if(CollectionTool.sizeNullSafe(nameValuePairs) < 1){ return null; }
		StringBuilder sb = new StringBuilder();
		int numAppended = 0;
		for(String nameValuePair : nameValuePairs){
			if(numAppended > 0){ sb.append(" and "); }
			sb.append(nameValuePair);
			++numAppended;
		}
		return sb.toString();
	}
	
	/**************************** serializable ******************/
	
	@Override
	public JSON getJson() {
		JSONObject j = new JSONObject();
		for(Field f : getFields()){
			j.element(f.getName(), f.getValue());
		}
		return j;
	}
	
	@SuppressWarnings("unchecked")
	public static Key<? extends Databean> fromJsonString(Class<?> clazz, String jsonString) {
		JSONObject jsonObject = (JSONObject)JSONSerializer.toJSON(jsonString);
		JsonConfig jsonConfig = new JsonConfig();  
		jsonConfig.setRootClass(clazz);  
		return (Key<? extends Databean>)JSONObject.toBean(jsonObject, jsonConfig );
	}
	
	
}

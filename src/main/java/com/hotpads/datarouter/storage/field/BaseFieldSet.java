package com.hotpads.datarouter.storage.field;

import java.util.List;

import com.hotpads.datarouter.storage.field.compare.FieldSetComparator;
import com.hotpads.datarouter.util.core.CollectionTool;

@SuppressWarnings("serial")
public abstract class BaseFieldSet<F extends FieldSet<F>> 
implements FieldSet<F>{
	
	
	/******** standard *************************/
	
	@Override
	public boolean equals(Object that){
		if(that==null){ return false; }
		if( ! (getClass().equals(that.getClass()))){ return false; }
		return 0 == compareTo((FieldSet<?>)that);
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
		return FieldSetComparator.compareStatic(this, that);
	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName()+"."+this.getPersistentString();
	}
	
	/**************************** serialize ******************/

	@Override
	public String getPersistentString(){
		return FieldSetTool.getPersistentString(getFields());
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
			field.setUsingReflection(this, field.getValue());
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
	
	//allows us to use the databean itself as the default Fielder
	//  - eliminates the less user-friendly nested class
	@Override
	public List<Field<?>> getFields(F f){
		return f.getFields();
	}
	
	
}













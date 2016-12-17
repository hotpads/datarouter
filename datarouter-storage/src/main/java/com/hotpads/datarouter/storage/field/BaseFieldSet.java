package com.hotpads.datarouter.storage.field;

import java.util.List;

import com.hotpads.datarouter.storage.field.compare.FieldSetComparator;

public abstract class BaseFieldSet<F extends FieldSet<F>>
implements FieldSet<F>{


	/******** standard *************************/

	@Override
	public boolean equals(Object that){
		if(that == null){
			return false;
		}
		if(!getClass().equals(that.getClass())){
			return false;
		}
		return 0 == compareTo((FieldSet<?>)that);
	}

	@Override
	public int hashCode(){
		int result = 0;
		for(Field<?> field : getFields()){
			result = 31 * result + field.getValueHashCode();
		}
		return result;
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
		return getClass().getSimpleName() + "." + getPersistentString();
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
	public List<Field<?>> getFields(F fieldset){
		return fieldset.getFields();
	}

	/**************************** serialize ******************/

	@Override
	@Deprecated //used in jsps.  replace with percent codec
	public String getPersistentString(){
		return FieldSetTool.getPersistentString(getFields());
	}

	@Override
	@Deprecated //replace with percent codec
	public String getTypedPersistentString(){
		return getClass().getSimpleName() + "_" + getPersistentString();
	}

	@Override
	@Deprecated //replace with percent codec
	public void fromPersistentString(String in){
		String[] tokens = in.split("_");
		int index = 0;
		for(Field<?> field : getFields()){
			if(index > tokens.length - 1){
				break;
			}
			field.fromString(tokens[index]);
			field.setUsingReflection(this, field.getValue());
			field.setValue(null);// to be safe until Field logic is cleaned up
			++index;
		}
	}

}

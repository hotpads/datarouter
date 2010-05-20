package com.hotpads.datarouter.storage.databean;

import java.util.List;

import com.hotpads.datarouter.serialize.JsonAware;
import com.hotpads.datarouter.serialize.JsonTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldTool;

@SuppressWarnings("serial")
public abstract class BaseFieldDatabean extends BaseDatabean implements FieldSet, JsonAware{

	/****************** standard *****************************/

//	@Override
//	public String toString(){
//		return getJson().toString();
//	}	
	
	/****************** fields ************************/
	
	public abstract List<Field<?>> getNonKeyFields();

	@Override
	public List<Field<?>> getFields(){
		List<Field<?>> allFields = this.getKey().getFields();
		allFields.addAll(this.getNonKeyFields());
		return allFields;
	}
	
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
	public String getJson() {
		return JsonTool.getJson(this.getFields()).toString();
	}
}

package com.hotpads.datarouter.storage.databean;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ClassTool;


@SuppressWarnings("serial")
public abstract class BaseDatabean 
implements Databean {

	@Override
	public String getDatabeanName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){ return false; }
		Databean that = (Databean)obj;
		return this.getKey().equals(that.getKey());
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Databean obj) {
		return this.getKey().compareTo(obj.getKey());
	}
	
	
	@Override
	public int hashCode(){
		return this.getKey().hashCode();
	}

	@Override
	public String toString(){
		return this.getClass().getSimpleName()+"."+this.getKey().getPersistentString();
	}
	
	public List<Field> getKeyFields(){
		return this.getKey().getFields();
	}
	
//	@Override
//	public List<Field> getFields(){
//		List<Field> keyFields = this.getKey().getFields();
//		List<Field> dataFields = this.getDataFields();
//		int totalFields = CollectionTool.size(keyFields) + CollectionTool.size(dataFields);
//		List<Field> fields = new ArrayList<Field>(totalFields);
//		fields.addAll(keyFields);
//		fields.addAll(dataFields);
//		return fields;
//	}
//	
//	@Override
//	public List<Field> getDataFields(){
//		return new LinkedList<Field>();
//	}
	
	
}

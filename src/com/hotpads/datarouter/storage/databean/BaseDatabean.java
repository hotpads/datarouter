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
	
	public List<Field<?>> getKeyFields(){
		return this.getKey().getFields();
	}
	
	
}

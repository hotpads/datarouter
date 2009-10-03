package com.hotpads.datarouter.storage.databean;

import com.hotpads.util.core.ClassTool;


@SuppressWarnings("serial")
public abstract class BaseDatabean 
implements Databean {

	@Override
	public String getDatabeanName() {
		return this.getClass().getSimpleName();
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Databean obj) {
		if(ClassTool.differentClass(this, obj)){ return 1; }
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
	
}

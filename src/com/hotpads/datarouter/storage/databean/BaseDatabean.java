package com.hotpads.datarouter.storage.databean;


@SuppressWarnings("serial")
public abstract class BaseDatabean 
implements Databean {

	@Override
	public String getDatabeanName() {
		return this.getClass().getSimpleName();
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Databean o) {
		return this.getKey().compareTo(o.getKey());
	}

	@Override
	public String toString(){
		return this.getClass().getSimpleName()+"."+this.getKey().getPersistentString();
	}
	
}

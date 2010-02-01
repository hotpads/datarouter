package com.hotpads.datarouter.storage.bundle;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;

public class SingleTypeBundle<D extends Databean> {

	protected NavigableSet<D> databeans = new TreeSet<D>();
	
	public void add(D databean){
		if(databean==null){ return; }
		this.databeans.add(databean);
	}

	public void add(Collection<D> databeans){
		for(D databean : CollectionTool.nullSafe(databeans)){
			this.add(databean);
		}
	}
	
	public NavigableSet<D> getDatabeans(){
		return this.databeans;
	}
	
	public D getFirst(){
		return CollectionTool.getFirst(this.databeans);
	}
	
}

package com.hotpads.datarouter.storage.bundle;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class SingleTypeBundle<D extends Databean<?,?>>{

	protected NavigableSet<D> databeans = new TreeSet<>();

	public void add(D databean){
		if(databean == null){
			return;
		}
		this.databeans.add(databean);
	}

	public void add(Collection<D> databeans){
		for(D databean : DrCollectionTool.nullSafe(databeans)){
			this.add(databean);
		}
	}

	public NavigableSet<D> getDatabeans(){
		return this.databeans;
	}

	public D getFirst(){
		return DrCollectionTool.getFirst(this.databeans);
	}

}

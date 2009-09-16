package com.hotpads.datarouter.storage.bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.MapTool;


public class Bundle{

	protected Map<String,SingleTypeBundle<? extends Databean>> bundleByType = MapTool.createHashMap();
	
	protected <D extends Databean> void add(D databean){
		this.ensureSingleTypeBundleExists(databean);
		@SuppressWarnings("unchecked")
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(databean.getClass().getName());
		singleTypeBundle.add(databean);
	}
	
	protected <D extends Databean> void add(Collection<D> databeans){
		for(D databean : CollectionTool.nullSafe(databeans)){
			this.add(databean);
		}
	}
	
	protected <D extends Databean> D getFirst(Class<D> clazz){
		if(clazz==null){ return null; }
		if(this.bundleByType.get(clazz.getName())==null){ return null; }
		@SuppressWarnings("unchecked")
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(clazz.getName());
		return singleTypeBundle.getFirst();
	}
	
	protected <D extends Databean> List<D> getAll(Class<D> clazz){
		if(clazz==null){ return null; }
		if(this.bundleByType.get(clazz.getName())==null){ return null; }
		@SuppressWarnings("unchecked")
		SingleTypeBundle<D> singleTypeBundle = (SingleTypeBundle<D>)this.bundleByType.get(clazz.getName());
		return new ArrayList<D>(singleTypeBundle.databeanByKey.values());
	}
	
	protected <D extends Databean> void ensureSingleTypeBundleExists(D databean){
		if(this.bundleByType.get(databean.getClass().getName())==null){
			this.bundleByType.put(databean.getClass().getName(), new SingleTypeBundle<D>());
		}
	}
	
	
	
	public static void main(String... args){
	}
}

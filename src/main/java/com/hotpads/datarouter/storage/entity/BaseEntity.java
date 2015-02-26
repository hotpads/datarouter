package com.hotpads.datarouter.storage.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public abstract class BaseEntity<EK extends EntityKey<EK>>
implements Entity<EK>{

	private EK key;
	private NavigableMap<String,EntitySection<EK,?,?>> databeansByQualifierPrefix;
	
	public BaseEntity(EK key){
		this.key = key;
		this.databeansByQualifierPrefix = new TreeMap<>();
	}
	
	@Override
	public void setKey(EK key){
		this.key = key;
	}
	
	@Override
	public EK getKey(){
		return key;
	}
	
	@Override
	public int getNumDatabeans(){
		int total = 0;
		for(EntitySection<EK,?,?> entitySection : databeansByQualifierPrefix.values()){
			total += entitySection.getNumDatabeans();
		}
		return total;
	}
	
	@SuppressWarnings("unchecked") 
	@Override
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	void addDatabeansForQualifierPrefixUnchecked(String qualifierPrefix, Collection<? extends Databean<?,?>> databeans){
		addDatabeansForQualifierPrefix(qualifierPrefix, (Collection<D>)databeans);
	}
	
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	void addDatabeansForQualifierPrefix(String qualifierPrefix, Collection<D> databeans){
		@SuppressWarnings("unchecked") //types enforced by subclasses
		EntitySection<EK,PK,D> section = (EntitySection<EK,PK,D>)databeansByQualifierPrefix.get(qualifierPrefix);
		if(section==null){
			section = new EntitySection<EK,PK,D>();
			databeansByQualifierPrefix.put(qualifierPrefix, section);
		}
		section.addAllAndReSort(databeans);
	}

	//custom table name
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	ArrayList<D> getDatabeansForQualifierPrefix(Class<D> databeanClass, String qualifierPrefix){
		EntitySection<EK,PK,D> section = (EntitySection<EK,PK,D>)databeansByQualifierPrefix.get(qualifierPrefix);
		return section==null ? null : section.getDatabeans();
	}
	
	
	/******************* inner class *****************************/
	
	public static class EntitySection<
			EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>>{

		protected ArrayList<D> databeans = new ArrayList<>();
		
//		public void addAndReSort(D databean){
//			if(databean==null){ return; }
//			databeans.add(databean);
//			Collections.sort(databeans);
//		}

		public void addAllAndReSort(Collection<D> toAdd){
			databeans.addAll(DrCollectionTool.nullSafe(toAdd));
			Collections.sort(databeans);
		}
		
		public ArrayList<D> getDatabeans(){
			return databeans;
		}
		
		public D getFirst(){
			return DrCollectionTool.getFirst(databeans);
		}
		
		public int getNumDatabeans(){
			return DrCollectionTool.size(databeans);
		}
		
	}

	
	

}

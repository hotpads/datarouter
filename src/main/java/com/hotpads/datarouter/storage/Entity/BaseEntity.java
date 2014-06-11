package com.hotpads.datarouter.storage.Entity;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.CollectionTool;

public abstract class BaseEntity<EK extends EntityKey<EK>> implements Entity<EK>{

	private EK key;
	private NavigableMap<String,EntitySection<EK,?,?>> databeansByNodeName;
	
	public BaseEntity(EK key){
		this.key = key;
		this.databeansByNodeName = new TreeMap<String,EntitySection<EK,?,?>>();
	}
	
	@Override
	public EK getKey(){
		return key;
	}

	@Override
	public NavigableMap<String,EntitySection<EK,?,?>> getDatabeansByNodeName(){
		return databeansByNodeName;
	}
	
	
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	void add(Node<PK,D> node, Collection<D> databeans){
		String nodeName = node.getName();
		@SuppressWarnings("unchecked") //types enforced externally
		EntitySection<EK,PK,D> section = (EntitySection<EK,PK,D>)databeansByNodeName.get(nodeName);
		if(section==null){
			section = new EntitySection<EK,PK,D>();
			databeansByNodeName.put(nodeName, section);
		}
		section.add(databeans);
	}
	
	
	/******************* inner class *****************************/
	
	public static class EntitySection<
			EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>>{

		protected NavigableSet<D> databeans = new TreeSet<D>();
		
		public void add(D databean){
			if(databean==null){ return; }
			databeans.add(databean);
		}

		public void add(Collection<D> databeans){
			for(D databean : CollectionTool.nullSafe(databeans)){
				add(databean);
			}
		}
		
		public NavigableSet<D> getDatabeans(){
			return databeans;
		}
		
		public D getFirst(){
			return CollectionTool.getFirst(databeans);
		}
		
	}

	
	

}

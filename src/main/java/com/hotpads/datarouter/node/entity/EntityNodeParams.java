package com.hotpads.datarouter.node.entity;

import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;

public class EntityNodeParams<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	private final String nodeName;
	private final Class<EK> entityKeyClass;
	private final Class<E> entityClass;
	private final Class<? extends EntityPartitioner<EK>> entityPartitionerClass;
	private final String entityTableName;
	
	
	public EntityNodeParams(String nodeName, Class<EK> entityKeyClass, 
			Class<E> entityClass, Class<? extends EntityPartitioner<EK>> entityPartitionerClass,
			String entityTableName){
		this.nodeName = nodeName;
		this.entityKeyClass = entityKeyClass;
		this.entityClass = entityClass;
		this.entityPartitionerClass = entityPartitionerClass;
		this.entityTableName = entityTableName;
	}
	
	
	public String getNodeName(){
		return nodeName;
	}
	
	public Class<EK> getEntityKeyClass(){
		return entityKeyClass;
	}
	
	public Class<E> getEntityClass(){
		return entityClass;
	}

	public Class<? extends EntityPartitioner<EK>> getEntityPartitionerClass(){
		return entityPartitionerClass;
	}

	public String getEntityTableName(){
		return entityTableName;
	}

}

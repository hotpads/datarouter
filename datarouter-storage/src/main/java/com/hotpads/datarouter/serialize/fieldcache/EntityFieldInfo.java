package com.hotpads.datarouter.serialize.fieldcache;

import java.util.List;

import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.entity.base.NoOpEntityPartitioner;
import com.hotpads.util.core.java.ReflectionTool;

public class EntityFieldInfo<
		EK extends EntityKey<EK>,
//		EP extends EntityPartitioner<EK>,
		E extends Entity<EK>>{
	
	public static final byte ENTITY_PREFIX_TERMINATOR = 0;

	private String entityTableName;
	private Class<EK> entityKeyClass;
	private EK sampleEntityKey;
//	private List<Field<?>> entityKeyFields;
	private Class<? extends EntityPartitioner<EK>> entityPartitionerClass;
	private EntityPartitioner<EK> entityPartitioner;
	private Class<E> entityClass;
	
	
	public EntityFieldInfo(EntityNodeParams<EK,E> params){
		this.entityTableName = params.getEntityTableName();
		this.entityKeyClass = params.getEntityKeyClass();
		this.sampleEntityKey = ReflectionTool.create(entityKeyClass);
		//careful as i think the PK may override these
//		this.entityKeyFields = sampleEntityKey.getFields();
		this.entityPartitionerClass = params.getEntityPartitionerClass();
		if(entityPartitionerClass==null){
			this.entityPartitioner = new NoOpEntityPartitioner<>();
		}else{
			this.entityPartitioner = ReflectionTool.create(entityPartitionerClass);
		}
		this.entityClass = params.getEntityClass();
	}

	
	
	public static byte getEntityPrefixTerminator(){
		return ENTITY_PREFIX_TERMINATOR;
	}

	public String getEntityTableName(){
		return entityTableName;
	}

	public Class<EK> getEntityKeyClass(){
		return entityKeyClass;
	}

	public EK getSampleEntityKey(){
		return sampleEntityKey;
	}

//	public List<Field<?>> getEntityKeyFields(){
//		return entityKeyFields;
//	}

	public Class<? extends EntityPartitioner<EK>> getEntityPartitionerClass(){
		return entityPartitionerClass;
	}

	public EntityPartitioner<EK> getEntityPartitioner(){
		return entityPartitioner;
	}

	public Class<E> getEntityClass(){
		return entityClass;
	}

}

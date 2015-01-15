package com.hotpads.datarouter.node;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.cache.Cached;

public class NodeParams<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	//required
	private final Datarouter router;
	private final String clientName;
	private final String parentName;
	private final Class<D> databeanClass;
	
	//sometimes optional, like with hibernate
	private final Class<F> fielderClass;
	
	//for schema evolution
	private final Integer schemaVersion;

	//sometimes we need to know the superclass of a databean
	private final Class<? super D> baseDatabeanClass;
	
	//name the table different than the databean class
	private final String physicalName;
	private final String qualifiedPhysicalName;//weird hibernate requirement ("entity name")
	
//	private final Class<? extends Entity<?>> entityClass;
//	private final Class<? extends EntityPartitioner<?>> entityPartitionerClass;
//	private final String entityTableName;
	private final String entityNodePrefix;
	
	//for proxy nodes (like http node)
	private final String remoteRouterName;
	private final String remoteNodeName;
	
	//diagnostics
	private final Cached<Boolean> recordCallsites;
	
	
	
	public NodeParams(Datarouter router, String clientName, String parentName, 
			Class<D> databeanClass, Class<F> fielderClass,
			Integer schemaVersion, Class<? super D> baseDatabeanClass, String physicalName, String qualifiedPhysicalName,
//			Class<? extends Entity<?>> entityClass, 
//			Class<? extends EntityPartitioner<?>> entityPartitionerClass, 
//			String entityTableName, 
			String entityNodePrefix, 
			String remoteRouterName, String remoteNodeName, 
			Cached<Boolean> recordCallsites){
		this.router = router;
		this.clientName = clientName;
		this.parentName = parentName;
		this.databeanClass = databeanClass;
		this.fielderClass = fielderClass;
		this.schemaVersion = schemaVersion;
		this.baseDatabeanClass = baseDatabeanClass;
		this.physicalName = physicalName;
		this.qualifiedPhysicalName = qualifiedPhysicalName;
//		this.entityClass = entityClass;
//		this.entityPartitionerClass = entityPartitionerClass;
//		this.entityTableName = entityTableName;
		this.entityNodePrefix = entityNodePrefix;
		this.remoteRouterName = remoteRouterName;
		this.remoteNodeName = remoteNodeName;
		this.recordCallsites = recordCallsites;
	}


	/******************** builder **************************/

	public static class NodeParamsBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>{
		private Datarouter router;
		private String parentName;
		private String clientName;
		private Class<D> databeanClass;
		
		private Class<F> fielderClass;
		
		private Integer schemaVersion;

		private Class<? super D> baseDatabeanClass;
		
		private String physicalName;
		private String qualifiedPhysicalName;
		
		private Class<? extends Entity<?>> entityClass;
		private Class<? extends EntityPartitioner<?>> entityPartitionerClass;
		private String entityTableName;
		private String entityNodePrefix;

		private String remoteRouterName;
		private String remoteNodeName;
		
		private Cached<Boolean> recordCallsites;
		
		/************** construct **************/
		
		public NodeParamsBuilder(Datarouter router, Class<D> databeanClass){
			this.router = router;
			this.databeanClass = databeanClass;
		}
		
		//save the caller from specifying generics
//		public static <
//				PK extends PrimaryKey<PK>,
//				D extends Databean<PK,D>,
//				F extends DatabeanFielder<PK,D>> 
//		NodeParamsBuilder<PK,D,F> create(DataRouter router, Class<D> databeanClass){
//			return new NodeParamsBuilder<PK,D,F>(router, databeanClass);
//		}
		
		
		/************* with *******************/

		public NodeParamsBuilder<PK,D,F> withClientName(String clientName){
			this.clientName = clientName;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withParentName(String parentName){
			this.parentName = parentName;
			return this;
		}
		
		public NodeParamsBuilder<PK,D,F> withFielder(Class<F> fielderClass){
			this.fielderClass = fielderClass;
			return this;
		}
		
		public NodeParamsBuilder<PK,D,F> withSchemaVersion(Integer schemaVersion){
			this.schemaVersion = schemaVersion;
			return this;
		}
		
		public NodeParamsBuilder<PK,D,F> withBaseDatabean(Class<? super D> baseDatabeanClass){
			this.baseDatabeanClass = baseDatabeanClass;
			return this;
		}
		
		public NodeParamsBuilder<PK,D,F> withTableName(String physicalName){
			this.physicalName = physicalName;
			return this;
		}
		
		public NodeParamsBuilder<PK,D,F> withHibernateTableName(String physicalName, String qualifiedPhysicalName){
			this.physicalName = physicalName;
			this.qualifiedPhysicalName = qualifiedPhysicalName;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withEntity(String entityTableName, String entityNodePrefix){
//			this.entityClass = entityClass;
//			this.entityPartitionerClass = entityPartitionerClass;
//			this.entityTableName = entityTableName;
			this.physicalName = entityTableName;
			this.entityNodePrefix = entityNodePrefix;
			return this;
		}
		
		public NodeParamsBuilder<PK,D,F> withProxyDestination(String remoteRouterName, String remoteNodeName){
			this.physicalName = remoteRouterName;
			this.qualifiedPhysicalName = remoteNodeName;
			return this;
		}
		
		public NodeParamsBuilder<PK,D,F> withDiagnostics(Cached<Boolean> recordCallsites){
			this.recordCallsites = recordCallsites;
			return this;
		}		
		
		
		/******************* build ***************************/
		
		public NodeParams<PK,D,F> build(){
			return new NodeParams<>(router, clientName, parentName, 
					databeanClass, fielderClass, schemaVersion, baseDatabeanClass,
					physicalName, qualifiedPhysicalName, 
//					entityClass, entityPartitionerClass, entityTableName, 
					entityNodePrefix, 
					remoteRouterName, remoteNodeName,
					recordCallsites);
		}
	}


	/*********** get ***********************/

	public Datarouter getRouter(){
		return router;
	}

	public String getClientName(){
		return clientName;
	}
	
	public String getParentName(){
		return parentName;
	}

	public Class<D> getDatabeanClass(){
		return databeanClass;
	}

	public Class<F> getFielderClass(){
		return fielderClass;
	}

	public Integer getSchemaVersion(){
		return schemaVersion;
	}

	public Class<? super D> getBaseDatabeanClass(){
		return baseDatabeanClass;
	}

	public String getPhysicalName(){
		return physicalName;
	}

	public String getQualifiedPhysicalName(){
		return qualifiedPhysicalName;
	}

	public String getRemoteRouterName(){
		return remoteRouterName;
	}

	public String getRemoteNodeName(){
		return remoteNodeName;
	}
	
//	public Class<? extends Entity<?>> getEntityClass(){
//		return entityClass;
//	}
	
//	public Class<? extends EntityPartitioner<?>> getEntityPartitionerClass(){
//		return entityPartitionerClass;
//	}

//	public String getEntityTableName(){
//		return entityTableName;
//	}

	public String getEntityNodePrefix(){
		return entityNodePrefix;
	}

	public Cached<Boolean> getRecordCallsites(){
		return recordCallsites;
	}
	
	
	
}

package com.hotpads.datarouter.node;

import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class NodeParams<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	//required
	private final DataRouter router;
	private final String clientName;
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
	
	//for proxy nodes (like http node)
	private final String remoteRouterName;
	private final String remoteNodeName;
	
	
	
	public NodeParams(DataRouter router, String clientName, Class<D> databeanClass, Class<F> fielderClass,
			Integer schemaVersion, Class<? super D> baseDatabeanClass, String physicalName, String qualifiedPhysicalName,
			String remoteRouterName, String remoteNodeName){
		this.router = router;
		this.clientName = clientName;
		this.databeanClass = databeanClass;
		this.fielderClass = fielderClass;
		this.schemaVersion = schemaVersion;
		this.baseDatabeanClass = baseDatabeanClass;
		this.physicalName = physicalName;
		this.qualifiedPhysicalName = qualifiedPhysicalName;
		this.remoteRouterName = remoteRouterName;
		this.remoteNodeName = remoteNodeName;
	}


	/******************** builder **************************/

	public static class NodeParamsBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>{
		private DataRouter router;
		private String clientName;
		private Class<D> databeanClass;
		
		private Class<F> fielderClass;
		
		private Integer schemaVersion;

		private Class<? super D> baseDatabeanClass;
		
		private String physicalName;
		private String qualifiedPhysicalName;

		private String remoteRouterName;
		private String remoteNodeName;
		
		
		/************** construct **************/
		
		@Deprecated//don't require clientName
		public NodeParamsBuilder(DataRouter router, String clientName, Class<D> databeanClass){
			this.router = router;
			this.clientName = clientName;
			this.databeanClass = databeanClass;
		}
		
		public NodeParamsBuilder(DataRouter router, Class<D> databeanClass){
			this.router = router;
			this.databeanClass = databeanClass;
		}
		
		
		/************* with *******************/

		public NodeParamsBuilder<PK,D,F> withClientName(String clientName){
			this.clientName = clientName;
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
		
		public NodeParamsBuilder<PK,D,F> withProxyDestination(String remoteRouterName, String remoteNodeName){
			this.physicalName = remoteRouterName;
			this.qualifiedPhysicalName = remoteNodeName;
			return this;
		}
		
		
		/******************* build ***************************/
		
		public NodeParams<PK,D,F> build(){
			return new NodeParams<>(router, clientName, databeanClass, fielderClass, schemaVersion, baseDatabeanClass,
					physicalName, qualifiedPhysicalName, remoteRouterName, remoteNodeName);
		}
	}


	/*********** get ***********************/

	public DataRouter getRouter(){
		return router;
	}

	public String getClientName(){
		return clientName;
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
	
	
	
}

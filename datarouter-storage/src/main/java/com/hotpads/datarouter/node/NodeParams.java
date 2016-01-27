package com.hotpads.datarouter.node;

import java.util.Optional;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.cache.Cached;
import com.hotpads.util.core.java.ReflectionTool;

public class NodeParams<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	//required
	private final Router router;
	private final ClientId clientId;
	private final String parentName;
	private final Supplier<D> databeanSupplier;
	private final String databeanName;

	//sometimes optional, like with hibernate
	private final Supplier<F> fielderSupplier;

	//for schema evolution
	private final Integer schemaVersion;

	//sometimes we need to know the superclass of a databean
	private final Class<? super D> baseDatabeanClass;

	//name the table different than the databean class
	private final String physicalName;
	private final String qualifiedPhysicalName;//weird hibernate requirement ("entity name")

	private final String entityNodePrefix;

	//for proxy nodes (like http node)
	private final String remoteRouterName;
	private final String remoteNodeName;

	//diagnostics
	private final Cached<Boolean> recordCallsites;

	public NodeParams(Router router, ClientId clientId, String parentName, Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier, Integer schemaVersion, Class<? super D> baseDatabeanClass, String physicalName,
			String qualifiedPhysicalName, String entityNodePrefix, String remoteRouterName, String remoteNodeName,
			Cached<Boolean> recordCallsites){
		this.router = router;
		this.clientId = clientId;
		this.parentName = parentName;
		this.databeanSupplier = databeanSupplier;
		this.databeanName = databeanSupplier.get().getDatabeanName();
		this.fielderSupplier = fielderSupplier;
		this.schemaVersion = schemaVersion;
		this.baseDatabeanClass = baseDatabeanClass;
		this.physicalName = physicalName;
		this.qualifiedPhysicalName = qualifiedPhysicalName;
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
		private Router router;
		private String parentName;
		private ClientId clientId;
		private Supplier<D> databeanSupplier;

		private Supplier<F> fielderSupplier;

		private Integer schemaVersion;

		private Class<? super D> baseDatabeanClass;

		private String physicalName;
		private String qualifiedPhysicalName;

		private String entityNodePrefix;

		private String remoteRouterName;
		private String remoteNodeName;

		private Cached<Boolean> recordCallsites;

		/************** construct **************/

		/**
		 * @deprecated use {@link #NodeParams(Router, Supplier)}
		 */
		@Deprecated
		public NodeParamsBuilder(Router router, Class<D> databeanClass){
			this(router, ReflectionTool.supplier(databeanClass));
		}

		public NodeParamsBuilder(Router router, Supplier<D> databeanSupplier){
			this.router = router;
			this.databeanSupplier = databeanSupplier;
		}

		/************* with *******************/

		public NodeParamsBuilder<PK,D,F> withClientId(ClientId clientId){
			this.clientId = clientId;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withParentName(String parentName){
			this.parentName = parentName;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withFielder(Supplier<F> fielderSupplier){
			this.fielderSupplier = fielderSupplier;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withFielder(Class<F> fielderClass){
			return withFielder(Optional.ofNullable(fielderClass).map(ReflectionTool::supplier).orElse(null));
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
			return new NodeParams<>(router, clientId, parentName,
					databeanSupplier, fielderSupplier, schemaVersion, baseDatabeanClass,
					physicalName, qualifiedPhysicalName,
					entityNodePrefix,
					remoteRouterName, remoteNodeName,
					recordCallsites);
		}
	}


	/*********** get ***********************/

	public Router getRouter(){
		return router;
	}

	public ClientId getClientId(){
		return clientId;
	}

	public String getClientName(){
		if(clientId == null){
			return null;
		}
		return clientId.getName();
	}

	public String getParentName(){
		return parentName;
	}

	public Supplier<D> getDatabeanSupplier(){
		return databeanSupplier;
	}

	public String getDatabeanName(){
		return databeanName;
	}

	public Supplier<F> getFielderSupplier(){
		return fielderSupplier;
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

	public String getEntityNodePrefix(){
		return entityNodePrefix;
	}

	public Cached<Boolean> getRecordCallsites(){
		return recordCallsites;
	}
}

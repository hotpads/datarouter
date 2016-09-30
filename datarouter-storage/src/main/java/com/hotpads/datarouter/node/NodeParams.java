package com.hotpads.datarouter.node;

import java.util.Optional;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

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

	//name the table different than the databean class
	private final String physicalName;
	private final Optional<String> namespace;

	private final String entityNodePrefix;

	//for proxy nodes (like http node)
	private final String remoteRouterName;
	private final String remoteNodeName;

	//diagnostics
	private final Setting<Boolean> recordCallsites;

	//for kinesis streams
	private final String streamName;
	private final String regionName;

	public NodeParams(Router router, ClientId clientId, String parentName, Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier, Integer schemaVersion, String physicalName, String namespace,
			String entityNodePrefix, String remoteRouterName, String remoteNodeName, Setting<Boolean> recordCallsites,
			String streamName, String regionName){
		this.router = router;
		this.clientId = clientId;
		this.parentName = parentName;
		this.databeanSupplier = databeanSupplier;
		this.namespace = Optional.ofNullable(namespace);
		this.databeanName = databeanSupplier.get().getDatabeanName();
		this.fielderSupplier = fielderSupplier;
		this.schemaVersion = schemaVersion;
		this.physicalName = physicalName;
		this.entityNodePrefix = entityNodePrefix;
		this.remoteRouterName = remoteRouterName;
		this.remoteNodeName = remoteNodeName;
		this.recordCallsites = recordCallsites;
		this.streamName = streamName;
		this.regionName = regionName;
	}


	/******************** builder **************************/

	public static class NodeParamsBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>{
		private final Router router;
		private final Supplier<D> databeanSupplier;
		private final Supplier<F> fielderSupplier;
		private String parentName;
		private ClientId clientId;
		private Integer schemaVersion;
		private String physicalName;
		private String namespace;
		private String entityNodePrefix;
		private String remoteRouterName;
		private String remoteNodeName;
		private Setting<Boolean> recordCallsites;

		private String streamName;
		private String regionName;


		/************** construct **************/

		public NodeParamsBuilder(Router router, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
			this.router = router;
			this.databeanSupplier = databeanSupplier;
			this.fielderSupplier = fielderSupplier;
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

		public NodeParamsBuilder<PK,D,F> withSchemaVersion(Integer schemaVersion){
			this.schemaVersion = schemaVersion;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withTableName(String physicalName){
			this.physicalName = physicalName;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withEntity(String entityTableName, String entityNodePrefix){
			this.physicalName = entityTableName;
			this.entityNodePrefix = entityNodePrefix;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withProxyDestination(String remoteRouterName){
			this.physicalName = remoteRouterName;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withDiagnostics(Setting<Boolean> recordCallsites){
			this.recordCallsites = recordCallsites;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withNamespace(String namespace){
			this.namespace = namespace;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withStreamName(String streamName){
			this.streamName = streamName;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withRegionName(String regionName){
			this.regionName = regionName;
			return this;
		}

		/******************* build ***************************/

		public NodeParams<PK,D,F> build(){
			return new NodeParams<>(router, clientId, parentName, databeanSupplier, fielderSupplier, schemaVersion,
					physicalName, namespace, entityNodePrefix, remoteRouterName, remoteNodeName, recordCallsites,
					streamName, regionName);
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

	public String getPhysicalName(){
		return physicalName;
	}

	public Optional<String> getNamespace(){
		return namespace;
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

	public Setting<Boolean> getRecordCallsites(){
		return recordCallsites;
	}

	public String getStreamName(){
		return streamName;
	}

	public String getRegionName(){
		return regionName;
	}
}

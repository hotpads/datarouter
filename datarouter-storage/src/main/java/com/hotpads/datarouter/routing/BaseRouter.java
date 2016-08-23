package com.hotpads.datarouter.routing;

import java.util.List;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.LazyClientProvider;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.FutureTool;

public abstract class BaseRouter
implements Router{

	public static final String
		MODE_development = "development",
		MODE_production = "production"
		;

	/********************************* fields **********************************/

	protected final Datarouter datarouter;
	private final String configLocation;
	private final String name;
	private final List<String> clientNames;
	private final RouterOptions routerOptions;
	private final NodeFactory nodeFactory;
	private final DatarouterSettings datarouterSettings;

	/**************************** constructor  ****************************************/

	public BaseRouter(Datarouter datarouter, String configLocation, String name, NodeFactory nodeFactory,
			DatarouterSettings datarouterSettings){
		this.datarouter = datarouter;
		this.configLocation = configLocation;
		this.name = name;
		this.datarouterSettings = datarouterSettings;
		this.clientNames = ClientId.getNames(getClientIds());
		this.routerOptions = new RouterOptions(getConfigLocation());
		this.datarouter.registerConfigFile(getConfigLocation());
		this.nodeFactory = nodeFactory;
		registerWithContext();
	}

	/**
	 * @deprecated use {@link #BaseRouter(Datarouter, String, String, NodeFactory, DatarouterSettings)}
	 */
	@Deprecated
	public BaseRouter(Datarouter datarouter, String configLocation, String name){
		this(datarouter, configLocation, name, null, null);
	}

	/********************************* methods *************************************/

	@Override
	public final String getConfigLocation(){
		return configLocation;
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>> N register(N node){
		datarouter.getNodes().register(name, node);
		datarouter.registerClientIds(node.getClientIds())
				.filter(LazyClientProvider::isInitialized)
				.map(LazyClientProvider::call)
				.map(client -> client.notifyNodeRegistration(node))
				.forEach(FutureTool::get);
		return node;
	}

	@Override
	public void registerWithContext(){
		datarouter.register(this);
	}

	/************************************** getting clients *************************/

	@Override
	public List<String> getClientNames(){
		return clientNames;
	}

	@Override
	public Client getClient(String clientName){
		return datarouter.getClientPool().getClient(clientName);
	}

	@Override
	public ClientType getClientType(String clientName){
		return datarouter.getClientPool().getClientTypeInstance(clientName);
	}

	@Override
	public List<Client> getAllClients(){
		return datarouter.getClientPool().getClients(getClientNames());
	}

	/***************** overexposed accessors *******************************/

	@Deprecated
	@Override
	public Datarouter getContext() {
		return datarouter;
	}


	/********************* Object ********************************/

	@Override
	public String toString(){
		return name;
	}

	@Override
	public int compareTo(Router otherDatarouter){
		return getName().compareToIgnoreCase(otherDatarouter.getName());
	}


	/********************* get/set ******************************/

	@Override
	public String getName() {
		return name;
	}

	@Override
	public RouterOptions getRouterOptions(){
		return routerOptions;
	}

	/* Node building */

	protected <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	NodeBuilder<PK,D,F> create(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
		return new NodeBuilder<>(clientId, databeanSupplier, fielderSupplier);
	}

	protected <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N createAndRegister(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
		return new NodeBuilder<>(clientId, databeanSupplier, fielderSupplier).buildAndRegister();
	}

	protected class NodeBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>{

		private final ClientId clientId;
		private final Supplier<D> databeanSupplier;
		private final Supplier<F> fielderSupplier;
		private String tableName;
		private Integer schemaVersion;

		private NodeBuilder(ClientId clientId, Supplier<D> databeanSupplier,
				Supplier<F> fielderSupplier){
			this.clientId = clientId;
			this.databeanSupplier = databeanSupplier;
			this.fielderSupplier = fielderSupplier;
		}

		public NodeBuilder<PK,D,F> withTableName(String tableName){
			this.tableName = tableName;
			return this;
		}

		public NodeBuilder<PK,D,F> withSchemaVersion(Integer schemaVersion){
			this.schemaVersion = schemaVersion;
			return this;
		}

		public <N extends NodeOps<PK,D>> N build(){
			NodeParams<PK,D,F> params = new NodeParamsBuilder<>(BaseRouter.this, databeanSupplier, fielderSupplier)
					.withClientId(clientId)
					.withTableName(tableName)
					.withSchemaVersion(schemaVersion)
					.withDiagnostics(datarouterSettings.getRecordCallsites())
					.build();
			return nodeFactory.create(params, true);
		}

		public <N extends NodeOps<PK,D>> N buildAndRegister(){
			return register(build());
		}

	}

}

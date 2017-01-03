package com.hotpads.datarouter.routing;

import java.util.List;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.LazyClientProvider;
import com.hotpads.datarouter.client.RouterOptions;
import com.hotpads.datarouter.client.imp.jdbc.node.index.TxnManagedUniqueIndexNode;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.factory.BaseNodeFactory;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage;
import com.hotpads.datarouter.node.type.index.UniqueIndexNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.FieldlessIndexEntry;
import com.hotpads.datarouter.storage.field.FieldlessIndexEntryFielder;
import com.hotpads.datarouter.storage.key.FieldlessIndexEntryPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.util.core.concurrent.FutureTool;

public abstract class BaseRouter
implements Router{

	public static final String
		MODE_development = "development",
		MODE_production = "production";


	protected final Datarouter datarouter;
	private final String configLocation;
	private final String name;
	private final RouterOptions routerOptions;
	private final BaseNodeFactory nodeFactory;
	private final DatarouterSettings datarouterSettings;


	public BaseRouter(Datarouter datarouter, String configLocation, String name, BaseNodeFactory nodeFactory,
			DatarouterSettings datarouterSettings){
		this.datarouter = datarouter;
		this.configLocation = configLocation;
		this.name = name;
		this.datarouterSettings = datarouterSettings;
		this.routerOptions = new RouterOptions(getConfigLocation());
		this.datarouter.registerConfigFile(getConfigLocation());
		this.nodeFactory = nodeFactory;
		registerWithContext();
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
				.flatMap(client -> node.getPhysicalNodesForClient(client.getName()).stream()
							.map(client::notifyNodeRegistration))
				.forEach(FutureTool::get);
		return node;
	}

	@Override
	public void registerWithContext(){
		datarouter.register(this);
	}

	/************************************** getting clients *************************/

	@Override
	public List<ClientId> getClientIds(){
		return datarouter.getNodes().getClientIdsForRouter(name);
	}

	@Override
	public List<String> getClientNames(){
		return ClientId.getNames(getClientIds());
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
	public Datarouter getContext(){
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
	public String getName(){
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

		private NodeBuilder(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
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

	protected <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>>
	ManagedNodeBuilder<PK,D,IK,FieldlessIndexEntry<IK,PK,D>,FieldlessIndexEntryFielder<IK,PK,D>>
	createKeyOnlyManagedIndex(Class<IK> indexEntryKeyClass, IndexedMapStorage<PK,D> backingNode){
		return new ManagedNodeBuilder<>(indexEntryKeyClass, () -> new FieldlessIndexEntry<>(indexEntryKeyClass),
				() -> new FieldlessIndexEntryFielder<>(indexEntryKeyClass), backingNode);
	}

	protected <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>>
	UniqueIndexNode<PK,D,IK,FieldlessIndexEntry<IK,PK,D>> buildKeyOnlyManagedIndex(Class<IK> indexEntryKeyClass,
			IndexedMapStorage<PK,D> backingNode){
		return createKeyOnlyManagedIndex(indexEntryKeyClass, backingNode).build();
	}

	protected class ManagedNodeBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			IK extends PrimaryKey<IK>,
			IE extends UniqueIndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>{

		private final Supplier<IE> databeanSupplier;
		private final Supplier<IF> fielderSupplier;
		private final IndexedMapStorage<PK,D> backingNode;
		private String tableName;

		public ManagedNodeBuilder(Class<IK> indexEntryKeyClass, Supplier<IE> databeanSupplier,
				Supplier<IF> fielderSupplier, IndexedMapStorage<PK,D> backingNode){
			this.databeanSupplier = databeanSupplier;
			this.fielderSupplier = fielderSupplier;
			this.backingNode = backingNode;
			this.tableName = indexEntryKeyClass.getSimpleName();
		}

		public ManagedNodeBuilder<PK,D,IK,IE,IF> withTableName(String tableName){
			this.tableName = tableName;
			return this;
		}

		public UniqueIndexNode<PK,D,IK,IE> build(){
			NodeParams<IK,IE,IF> params = new NodeParamsBuilder<>(BaseRouter.this, databeanSupplier, fielderSupplier)
					.withTableName(tableName)
					.build();
			return backingNode.registerManaged(new TxnManagedUniqueIndexNode<>(backingNode, params, tableName));
		}

	}

}

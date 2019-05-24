/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.router;

import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.FieldlessIndexEntryFielder;
import io.datarouter.model.index.unique.UniqueIndexEntry;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.entity.base.DefaultEntityPartitioner;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientManager;
import io.datarouter.storage.client.imp.TxnManagedUniqueIndexNode;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.entity.DefaultEntity;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.factory.BaseNodeFactory;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.op.combo.IndexedMapStorage;
import io.datarouter.storage.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import io.datarouter.storage.node.type.index.UniqueIndexNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.OptionalTool;
import io.datarouter.util.concurrent.FutureTool;

public abstract class BaseRouter implements Router, NodeSupport{

	public static final String MODE_development = "development";
	public static final String MODE_production = "production";

	private final Datarouter datarouter;
	private final BaseNodeFactory nodeFactory;
	private final DatarouterSettings datarouterSettings;

	public BaseRouter(Datarouter datarouter, DatarouterProperties datarouterProperties, BaseNodeFactory nodeFactory,
			DatarouterSettings datarouterSettings){
		this.datarouter = datarouter;
		this.datarouterSettings = datarouterSettings;
		this.nodeFactory = nodeFactory;
		datarouter.registerConfigFile(datarouterProperties.getDatarouterPropertiesFileLocation());
		datarouter.register(this);
	}

	/*---------------------------- methods ----------------------------------*/

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D,F>>
	N register(N node){
		datarouter.getNodes().register(node);
		for(ClientId clientId : datarouter.registerClientIds(node.getClientIds())){
			ClientManager clientManager = datarouter.getClientPool().getClientManager(clientId);
			node.getPhysicalNodesForClient(clientId.getName()).stream()
					.map(clientManager::notifyNodeRegistration)
					.map(FutureTool::get)
					.flatMap(OptionalTool::stream)
					.map(result -> result.errorMessage)
					.flatMap(OptionalTool::stream)
					.findFirst()
					.ifPresent($ -> clientManager.sendEmail());
		}
		return node;
	}

	/* Node building */

	protected <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	NodeBuilder<PK,D,F> create(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
		return new NodeBuilder<>(clientId, databeanSupplier, fielderSupplier);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createAndBuild(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
		return new NodeBuilder<>(clientId, databeanSupplier, fielderSupplier).build();
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D,F>>
	N createAndRegister(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
		return new NodeBuilder<>(clientId, databeanSupplier, fielderSupplier).buildAndRegister();
	}

	protected <
			PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SubEntityNodeBuilder<PK,D,F> createSubEntity(ClientId clientId, Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
			return new SubEntityNodeBuilder<>(clientId, databeanSupplier, fielderSupplier);
	}

	protected <
			PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N createAndRegisterSubEntity(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
		return new SubEntityNodeBuilder<>(clientId, databeanSupplier, fielderSupplier).buildAndRegister();
	}

	protected <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	QueueNodeBuilder<PK,D,F> createSingleQueue(ClientId clientId, Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new SingleQueueNodeBuilder<>(clientId, databeanSupplier, fielderSupplier);
	}

	protected <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	QueueNodeBuilder<PK,D,F> createGroupQueue(ClientId clientId, Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new GroupQueueNodeBuilder<>(clientId, databeanSupplier, fielderSupplier);
	}

	protected <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends NodeOps<PK,D>>
	N createAndRegisterGroupQueue(ClientId clientId, Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		return new GroupQueueNodeBuilder<>(clientId, databeanSupplier, fielderSupplier).buildAndRegister();
	}

	protected class SubEntityNodeBuilder<
			PK extends RegularPrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>{

		private final ClientId clientId;
		private final Supplier<D> databeanSupplier;
		private final Supplier<F> fielderSupplier;
		private String tableName;
		private Integer schemaVersion;

		private SubEntityNodeBuilder(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
			this.clientId = clientId;
			this.databeanSupplier = databeanSupplier;
			this.fielderSupplier = fielderSupplier;
		}

		public SubEntityNodeBuilder<PK,D,F> withTableName(String tableName){
			this.tableName = tableName;
			return this;
		}

		public SubEntityNodeBuilder<PK,D,F> withSchemaVersion(Integer schemaVersion){
			this.schemaVersion = schemaVersion;
			return this;
		}

		public <N extends NodeOps<PK,D>> N build(){
			String databeanName = databeanSupplier.get().getDatabeanName();
			String entityName = databeanName + "Entity";
			String entityNodePrefix = databeanName.replaceAll("[a-z]", "");
			NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
					.withClientId(clientId)
					.withDiagnostics(datarouterSettings.getRecordCallsites())
					.withEntity(entityName, entityNodePrefix)
					.withParentName(entityName)
					.withTableName(databeanName)
					.withTableName(tableName != null ? tableName : databeanName)
					.withSchemaVersion(schemaVersion)
					.build();
			Class<PK> keyClass = params.getDatabeanSupplier().get().getKeyClass();
			EntityNodeParams<PK,DefaultEntity<PK>> entityNodeParams = new EntityNodeParams<>(clientId.getName() + "."
					+ entityName, keyClass, DefaultEntity.supplier(keyClass), DefaultEntityPartitioner::new,
					entityName);
			return nodeFactory.createSubEntity(entityNodeParams, params);
		}

		public <N extends NodeOps<PK,D>> N buildAndRegister(){
			return register(build());
		}

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
			NodeParams<PK,D,F> params = new NodeParamsBuilder<>(databeanSupplier, fielderSupplier)
					.withClientId(clientId)
					.withTableName(tableName)
					.withSchemaVersion(schemaVersion)
					.withDiagnostics(datarouterSettings.getRecordCallsites())
					.build();
			return nodeFactory.create(params);
		}

		public <N extends NodeOps<PK,D>> N buildAndRegister(){
			return register(build());
		}

	}

	protected <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>>
	ManagedNodeBuilder<PK,D,IK,FieldlessIndexEntry<IK,PK,D>,FieldlessIndexEntryFielder<IK,PK,D>>

	createKeyOnlyManagedIndex(Class<IK> indexEntryKeyClass, IndexedMapStorageNode<PK,D,F> backingNode){
		return new ManagedNodeBuilder<>(indexEntryKeyClass, () -> new FieldlessIndexEntry<>(indexEntryKeyClass),
				() -> new FieldlessIndexEntryFielder<>(indexEntryKeyClass, backingNode.getFieldInfo()
						.getSampleFielder()), backingNode);
	}

	protected <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			IK extends FieldlessIndexEntryPrimaryKey<IK,PK,D>>
	UniqueIndexNode<PK,D,IK,FieldlessIndexEntry<IK,PK,D>> buildKeyOnlyManagedIndex(Class<IK> indexEntryKeyClass,
			IndexedMapStorageNode<PK,D,F> backingNode){
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
		private String indexName;

		public ManagedNodeBuilder(Class<IK> indexEntryKeyClass, Supplier<IE> databeanSupplier,
				Supplier<IF> fielderSupplier, IndexedMapStorage<PK,D> backingNode){
			this.databeanSupplier = databeanSupplier;
			this.fielderSupplier = fielderSupplier;
			this.backingNode = backingNode;
			this.indexName = indexEntryKeyClass.getSimpleName();
		}

		public ManagedNodeBuilder<PK,D,IK,IE,IF> withTableName(String tableName){
			this.indexName = tableName;
			return this;
		}

		public UniqueIndexNode<PK,D,IK,IE> build(){
			IndexEntryFieldInfo<IK,IE,IF> fieldInfo = new IndexEntryFieldInfo<>(indexName, databeanSupplier,
					fielderSupplier);
			return backingNode.registerManaged(new TxnManagedUniqueIndexNode<>(backingNode, fieldInfo, indexName));
		}

	}

	protected abstract class QueueNodeBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>{

		protected final ClientId clientId;
		protected final Supplier<D> databeanSupplier;
		protected final Supplier<F> fielderSupplier;

		protected String queueName;
		protected String namespace;
		protected String queueUrl;

		private QueueNodeBuilder(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
			this.clientId = clientId;
			this.databeanSupplier = databeanSupplier;
			this.fielderSupplier = fielderSupplier;
		}

		public QueueNodeBuilder<PK,D,F> withQueueName(String queueName){
			this.queueName = queueName;
			return this;
		}

		public QueueNodeBuilder<PK,D,F> withNamespace(String namespace){
			this.namespace = namespace;
			return this;
		}

		public QueueNodeBuilder<PK,D,F> withQueueUrl(String queueUrl){
			this.queueUrl = queueUrl;
			return this;
		}

		protected abstract <N extends NodeOps<PK,D>> N build();

		public <N extends NodeOps<PK,D>> N buildAndRegister(){
			return register(build());
		}

	}

	protected class SingleQueueNodeBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends QueueNodeBuilder<PK,D,F>{

		private SingleQueueNodeBuilder(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
			super(clientId, databeanSupplier, fielderSupplier);
		}

		@Override
		public <N extends NodeOps<PK,D>> N build(){
			return nodeFactory.createSingleQueueNode(clientId, databeanSupplier, queueName, fielderSupplier, namespace,
					queueUrl);
		}

	}

	protected class GroupQueueNodeBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends QueueNodeBuilder<PK,D,F>{

		private GroupQueueNodeBuilder(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
			super(clientId, databeanSupplier, fielderSupplier);
		}

		@Override
		public <N extends NodeOps<PK,D>> N build(){
			return nodeFactory.createGroupQueueNode(clientId, databeanSupplier, queueName, fielderSupplier, namespace,
					queueUrl);
		}

	}

}

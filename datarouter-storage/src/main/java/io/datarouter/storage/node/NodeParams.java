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
package io.datarouter.storage.node;

import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.setting.Setting;

public class NodeParams<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	//required
	private final ClientId clientId;
	private final String parentName;
	private final Supplier<D> databeanSupplier;
	private final String databeanName;
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

	//for external sqs
	private final String queueUrl;

	private NodeParams(ClientId clientId, String parentName, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier,
			Integer schemaVersion, String physicalName, String namespace, String entityNodePrefix,
			String remoteRouterName, String remoteNodeName, Setting<Boolean> recordCallsites, String streamName,
			String queueUrl){
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
		this.queueUrl = queueUrl;
	}


	/*----------------------------- builder ---------------------------------*/

	public static class NodeParamsBuilder<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>{
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

		private String queueUrl;


		/*--------------------------- construct -----------------------------*/

		public NodeParamsBuilder(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
			this.databeanSupplier = databeanSupplier;
			this.fielderSupplier = fielderSupplier;
		}

		/*---------------------------- with ---------------------------------*/

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

		public NodeParamsBuilder<PK,D,F> withQueueUrl(String queueUrl){
			this.queueUrl = queueUrl;
			return this;
		}

		/*----------------------------- build -------------------------------*/

		public NodeParams<PK,D,F> build(){
			return new NodeParams<>(clientId, parentName, databeanSupplier, fielderSupplier, schemaVersion,
					physicalName, namespace, entityNodePrefix, remoteRouterName, remoteNodeName, recordCallsites,
					streamName, queueUrl);
		}
	}

	/*-------------------------------- get ----------------------------------*/

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

	public String getQueueUrl(){
		return queueUrl;
	}

}

/*
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

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.blockfile.BlockfileNodeParams;
import io.datarouter.storage.node.tableconfig.NodewatchConfiguration;
import io.datarouter.storage.privacy.DatarouterPrivacyExemptionReason;
import io.datarouter.storage.privacy.DatarouterPrivacyProcessor;
import io.datarouter.storage.tag.Tag;
import io.datarouter.storage.util.Subpath;

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
	private final Supplier<String> schemaVersionSupplier;

	//name the table different than the databean class
	private final String physicalName;
	private final Optional<String> namespace;
	private final Supplier<Subpath> pathSupplier;

	private final String entityNodePrefix;

	//for proxy nodes (like http node)
	private final String remoteRouterName;
	private final String remoteNodeName;

	//diagnostics
	private final Supplier<Boolean> recordCallsites;

	//for kinesis streams
	private final String streamName;

	//for external sqs
	private final String queueUrl;

	//for nodewatch
	private final NodewatchConfiguration nodewatchConfiguration;

	private final boolean disableForcePrimary;

	// indicate if is a table category
	private final Tag tag;

	// for mysql utf8 to utf8mb4 migrations
	private final boolean disableIntroducer;

	private final Duration customMessageAgeThreshold;

	private final BlockfileNodeParams<PK,D,F> blockfileNodeParams;

	private final List<Class<? extends DatarouterPrivacyProcessor>> privacyProcessors;
	private final Optional<DatarouterPrivacyExemptionReason> privacyExemptionReason;

	private NodeParams(
			ClientId clientId,
			String parentName,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			Supplier<String> schemaVersionSupplier,
			String physicalName,
			String namespace,
			Supplier<Subpath> pathSupplier,
			String entityNodePrefix,
			String remoteRouterName,
			String remoteNodeName,
			Supplier<Boolean> recordCallsites,
			String streamName,
			String queueUrl,
			NodewatchConfiguration nodewatchConfiguration,
			boolean disableForcePrimary,
			Tag tag,
			boolean disableIntroducer,
			Duration customMessageAgeThreshold,
			BlockfileNodeParams<PK,D,F> blockfileNodeParams,
			List<Class<? extends DatarouterPrivacyProcessor>> privacyProcessors,
			Optional<DatarouterPrivacyExemptionReason> privacyExemptionReason){
		this.clientId = clientId;
		this.parentName = parentName;
		this.databeanSupplier = databeanSupplier;
		this.namespace = Optional.ofNullable(namespace);
		this.pathSupplier = Objects.requireNonNull(pathSupplier);
		this.databeanName = databeanSupplier.get().getDatabeanName();
		this.fielderSupplier = fielderSupplier;
		this.schemaVersionSupplier = Objects.requireNonNull(schemaVersionSupplier);
		this.physicalName = physicalName;
		this.entityNodePrefix = entityNodePrefix;
		this.remoteRouterName = remoteRouterName;
		this.remoteNodeName = remoteNodeName;
		this.recordCallsites = recordCallsites;
		this.streamName = streamName;
		this.queueUrl = queueUrl;
		this.nodewatchConfiguration = nodewatchConfiguration;
		this.disableForcePrimary = disableForcePrimary;
		this.tag = tag;
		this.disableIntroducer = disableIntroducer;
		this.customMessageAgeThreshold = customMessageAgeThreshold;
		this.blockfileNodeParams = blockfileNodeParams;
		this.privacyProcessors = privacyProcessors;
		this.privacyExemptionReason = privacyExemptionReason;
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
		private Supplier<String> schemaVersionSupplier = () -> null;
		private String physicalName;
		private String namespace;
		private Supplier<Subpath> pathSupplier = () -> null;
		private String entityNodePrefix;
		private String remoteRouterName;
		private String remoteNodeName;
		private Supplier<Boolean> recordCallsites;
		private String streamName;
		private String queueUrl;
		private NodewatchConfiguration nodewatchConfiguration;
		private boolean disableForcePrimary;
		private Tag tag;
		private boolean disableIntroducer;
		private Duration customMessageAgeThreshold = Duration.ofDays(2);
		private BlockfileNodeParams<PK,D,F> blockfileNodeParams;
		private List<Class<? extends DatarouterPrivacyProcessor>> privacyProcessors = List.of();
		private Optional<DatarouterPrivacyExemptionReason> privacyExemptionReason = Optional.empty();

		/*--------------------------- construct -----------------------------*/

		public NodeParamsBuilder(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier){
			this.databeanSupplier = databeanSupplier;
			this.fielderSupplier = fielderSupplier;
		}

		public NodeParamsBuilder(NodeParams<PK,D,F> params){
			databeanSupplier = params.databeanSupplier;
			fielderSupplier = params.fielderSupplier;
			parentName = params.parentName;
			clientId = params.clientId;
			schemaVersionSupplier = params.schemaVersionSupplier;
			physicalName = params.physicalName;
			namespace = params.namespace.orElse(null);
			pathSupplier = params.pathSupplier;
			entityNodePrefix = params.entityNodePrefix;
			remoteRouterName = params.remoteRouterName;
			remoteNodeName = params.remoteNodeName;
			recordCallsites = params.recordCallsites;
			streamName = params.streamName;
			queueUrl = params.queueUrl;
			nodewatchConfiguration = params.nodewatchConfiguration;
			disableForcePrimary = params.disableForcePrimary;
			tag = params.tag;
			disableIntroducer = params.disableIntroducer;
			customMessageAgeThreshold = params.customMessageAgeThreshold;
			blockfileNodeParams = params.blockfileNodeParams;
			privacyProcessors = params.privacyProcessors;
			privacyExemptionReason = params.privacyExemptionReason;
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

		public NodeParamsBuilder<PK,D,F> withSchemaVersionSupplier(Supplier<String> schemaVersionSupplier){
			this.schemaVersionSupplier = schemaVersionSupplier;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withBucketName(String physicalName){
			this.physicalName = physicalName;
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

		public NodeParamsBuilder<PK,D,F> withDiagnostics(Supplier<Boolean> recordCallsites){
			this.recordCallsites = recordCallsites;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withNamespace(String namespace){
			this.namespace = namespace;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withPath(Subpath path){
			return withPathSupplier(() -> path);
		}

		public NodeParamsBuilder<PK,D,F> withPathSupplier(Supplier<Subpath> pathSupplier){
			this.pathSupplier = pathSupplier;
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

		public NodeParamsBuilder<PK,D,F> withTableConfiguration(NodewatchConfiguration nodewatchConfiguration){
			this.nodewatchConfiguration = nodewatchConfiguration;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withDisableForcePrimary(boolean disableForcePrimary){
			this.disableForcePrimary = disableForcePrimary;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withTag(Tag tag){
			this.tag = tag;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withDisableIntroducer(boolean disableIntroducer){
			this.disableIntroducer = disableIntroducer;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withCustomMessageAgeThreshold(Duration customMessageAgeThreshold){
			this.customMessageAgeThreshold = customMessageAgeThreshold;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withBlockfileNodeParams(BlockfileNodeParams<PK,D,F> blockfileNodeParams){
			this.blockfileNodeParams = blockfileNodeParams;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withPrivacyProcessors(
				List<Class<? extends DatarouterPrivacyProcessor>> privacyProcessors){
			this.privacyProcessors = privacyProcessors;
			return this;
		}

		public NodeParamsBuilder<PK,D,F> withPrivacyExemptionReason(
				Optional<DatarouterPrivacyExemptionReason> privacyExemptionReason){
			this.privacyExemptionReason = privacyExemptionReason;
			return this;
		}

		/*----------------------------- build -------------------------------*/

		public NodeParams<PK,D,F> build(){
			return new NodeParams<>(
					clientId,
					parentName,
					databeanSupplier,
					fielderSupplier,
					schemaVersionSupplier,
					physicalName,
					namespace,
					pathSupplier,
					entityNodePrefix,
					remoteRouterName,
					remoteNodeName,
					recordCallsites,
					streamName,
					queueUrl,
					nodewatchConfiguration,
					disableForcePrimary,
					tag,
					disableIntroducer,
					customMessageAgeThreshold,
					blockfileNodeParams,
					privacyProcessors,
					privacyExemptionReason);
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

	public Supplier<String> getSchemaVersionSupplier(){
		return schemaVersionSupplier;
	}

	public String getPhysicalName(){
		return physicalName;
	}

	public Optional<String> getNamespace(){
		return namespace;
	}

	public Supplier<Subpath> getPathSupplier(){
		return pathSupplier;
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

	public Supplier<Boolean> getRecordCallsites(){
		return recordCallsites;
	}

	public String getStreamName(){
		return streamName;
	}

	public String getQueueUrl(){
		return queueUrl;
	}

	public NodewatchConfiguration getTableConfiguration(){
		return nodewatchConfiguration;
	}

	public boolean getDisableForcePrimary(){
		return disableForcePrimary;
	}

	@Deprecated
	public boolean getIsSystemTable(){
		return tag == Tag.DATAROUTER;
	}

	public Tag getTag(){
		return tag;
	}

	public boolean getDisableIntroducer(){
		return disableIntroducer;
	}

	public Duration getCustomMessageAgeThreshold(){
		return customMessageAgeThreshold;
	}

	public BlockfileNodeParams<PK,D,F> getBlockfileNodeParams(){
		return blockfileNodeParams;
	}

	public List<Class<? extends DatarouterPrivacyProcessor>> getPrivacyProcessors(){
		return privacyProcessors;
	}

	public Optional<DatarouterPrivacyExemptionReason> getPrivacyExemptionReason(){
		return privacyExemptionReason;
	}

}

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
package io.datarouter.storage.profile.trace.node;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.config.setting.impl.ProfilingSettings;
import io.datarouter.storage.node.entity.EntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.SubEntitySortedMapStorageNode;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.node.op.raw.GroupQueueStorage;
import io.datarouter.storage.node.op.raw.write.StorageWriter;
import io.datarouter.storage.profile.trace.Trace;
import io.datarouter.storage.profile.trace.Trace.TraceFielder;
import io.datarouter.storage.profile.trace.TraceEntity;
import io.datarouter.storage.profile.trace.TraceSpan;
import io.datarouter.storage.profile.trace.TraceSpan.TraceSpanFielder;
import io.datarouter.storage.profile.trace.TraceThread;
import io.datarouter.storage.profile.trace.TraceThread.TraceThreadFielder;
import io.datarouter.storage.profile.trace.key.TraceEntityKey;
import io.datarouter.storage.profile.trace.key.TraceEntityKey.TraceEntityPartitioner;
import io.datarouter.storage.profile.trace.key.TraceKey;
import io.datarouter.storage.profile.trace.key.TraceSpanKey;
import io.datarouter.storage.profile.trace.key.TraceThreadKey;
import io.datarouter.storage.routing.BaseRouter;
import io.datarouter.storage.routing.Datarouter;
import io.datarouter.storage.setting.Setting;

@Singleton
public class TraceRouter extends BaseRouter implements TraceNodes{

	public static class TraceEntityRouterParams{
		private final String configFileLocation;
		private final ClientId entityClientId;
		private final ClientId queueClientId;

		public TraceEntityRouterParams(String configFileLocation, ClientId entityClientId, ClientId queueClientId){
			this.configFileLocation = configFileLocation;
			this.entityClientId = entityClientId;
			this.queueClientId = queueClientId;
		}
	}

	public static final EntityNodeParams<TraceEntityKey,TraceEntity> ENTITY_NODE_PARAMS_TraceEntity16
			= new EntityNodeParams<>("TraceEntity16", TraceEntityKey.class, TraceEntity::new,
					TraceEntityPartitioner::new, "TraceEntity16");

	private final Setting<Boolean> bufferTracesInSqs;

	private final EntityNode<TraceEntityKey,TraceEntity> entity;
	private final SubEntitySortedMapStorageNode<TraceEntityKey,TraceKey,Trace,TraceFielder> trace;
	private final SubEntitySortedMapStorageNode<TraceEntityKey,TraceThreadKey,TraceThread,TraceThreadFielder>
			traceThread;
	private final SubEntitySortedMapStorageNode<TraceEntityKey,TraceSpanKey,TraceSpan,TraceSpanFielder> traceSpan;
	private final GroupQueueStorage<TraceKey,Trace> traceQueue;
	private final GroupQueueStorage<TraceThreadKey,TraceThread> traceThreadQueue;
	private final GroupQueueStorage<TraceSpanKey,TraceSpan> traceSpanQueue;

	@Inject
	public TraceRouter(Datarouter datarouter, EntityNodeFactory entityNodeFactory, QueueNodeFactory queueNodeFactory,
			NodeFactory nodeFactory, DatarouterSettings datarouterSettings, ProfilingSettings profilingSettings,
			TraceEntityRouterParams params){
		super(datarouter, params.configFileLocation, "traceEntity", nodeFactory, datarouterSettings);
		this.bufferTracesInSqs = profilingSettings.getBufferTracesInSqs();

		entity = entityNodeFactory.create(params.entityClientId.getName(), this, ENTITY_NODE_PARAMS_TraceEntity16);

		trace = register(nodeFactory.subEntityNode(ENTITY_NODE_PARAMS_TraceEntity16, params.entityClientId, Trace::new,
				TraceFielder::new, TraceEntity.QUALIFIER_PREFIX_Trace));
		entity.register(trace);

		traceThread = register(nodeFactory.subEntityNode(ENTITY_NODE_PARAMS_TraceEntity16, params.entityClientId,
				TraceThread::new, TraceThreadFielder::new, TraceEntity.QUALIFIER_PREFIX_TraceThread));
		entity.register(traceThread);

		traceSpan = register(nodeFactory.subEntityNode(ENTITY_NODE_PARAMS_TraceEntity16, params.entityClientId,
				TraceSpan::new, TraceSpanFielder::new, TraceEntity.QUALIFIER_PREFIX_TraceSpan));
		entity.register(traceSpan);

		traceQueue = register(queueNodeFactory.createGroupQueueNode(params.queueClientId, Trace::new, null,
				TraceFielder::new, true));
		traceThreadQueue = register(queueNodeFactory.createGroupQueueNode(params.queueClientId, TraceThread::new, null,
				TraceThreadFielder::new, true));
		traceSpanQueue = register(queueNodeFactory.createGroupQueueNode(params.queueClientId, TraceSpan::new, null,
				TraceSpanFielder::new, true));
	}

	@Override
	public EntityNode<TraceEntityKey,TraceEntity> entity(){
		return entity;
	}

	@Override
	public SortedMapStorage<TraceKey,Trace> trace(){
		return trace;
	}

	@Override
	public SortedMapStorage<TraceThreadKey,TraceThread> traceThread(){
		return traceThread;
	}

	@Override
	public SortedMapStorage<TraceSpanKey,TraceSpan> traceSpan(){
		return traceSpan;
	}

	@Override
	public StorageWriter<TraceKey,Trace> traceWriteQueue(){
		return bufferTracesInSqs.getValue() ? traceQueue : trace();
	}

	@Override
	public StorageWriter<TraceThreadKey,TraceThread> traceThreadWriteQueue(){
		return bufferTracesInSqs.getValue() ? traceThreadQueue : traceThread();
	}

	@Override
	public StorageWriter<TraceSpanKey,TraceSpan> traceSpanWriteQueue(){
		return bufferTracesInSqs.getValue() ? traceSpanQueue : traceSpan();
	}

	@Override
	public GroupQueueStorage<TraceKey,Trace> traceReadQueue(){
		return traceQueue;
	}

	@Override
	public GroupQueueStorage<TraceThreadKey,TraceThread> traceThreadReadQueue(){
		return traceThreadQueue;
	}

	@Override
	public GroupQueueStorage<TraceSpanKey,TraceSpan> traceSpanReadQueue(){
		return traceSpanQueue;
	}

}

package com.hotpads.job.record;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.record.LongRunningTask.LongRunningTaskFielder;

@Singleton
public class LongRunningTaskNodeProvider extends BaseRouter{

	public static final String CONFIG_LOCATION = "configLocation";
	public static final String DATAROUTER_JOB = "datarouterJob";

	public final IndexedSortedMapStorageNode<LongRunningTaskKey,LongRunningTask> longRunningTask;

	@Inject
	public LongRunningTaskNodeProvider(Datarouter datarouter, @Named(CONFIG_LOCATION) String configLocation,
			NodeFactory nodeFactory, DatarouterSettings datarouterSettings, @Named(DATAROUTER_JOB) ClientId clientId){
		super(datarouter, configLocation, "datarouterJob", nodeFactory, datarouterSettings);
		longRunningTask = createAndRegister(clientId, LongRunningTask::new, LongRunningTaskFielder::new);
	}

	public IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> get(){
		return longRunningTask;
	}

}

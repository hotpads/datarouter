package com.hotpads.datarouter.storage.failover;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class FailoverStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> implements StorageWriter<PK,D>{

	@Singleton
	public static class FailoverStorageWriterFactory{

		@Inject
		private FailoverRecoveringService failoverRecoveringService;
		@Inject
		private FailoverSettings failoverSettings;

		public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> FailoverStorageWriter<PK,D>
				createWithStandbyQueueStorage(StorageWriterNode<PK,D> mainStorage, QueueStorage<PK,D> standbyStorage){
			FailoverStorageWriter<PK,D> failoverStorage = new FailoverStorageWriter<>(mainStorage, standbyStorage,
					failoverSettings.shouldFailover(mainStorage.getName()));
			failoverRecoveringService.registerRecoveryPolicy(mainStorage, () -> standbyStorage.poll(null));
			return failoverStorage;
		}

	}

	private final StorageWriter<PK,D> mainStorage;
	private final StorageWriter<PK,D> standbyStorage;
	private final Setting<Boolean> shouldFailover;

	public FailoverStorageWriter(StorageWriter<PK,D> mainStorage, StorageWriter<PK,D> standbyStorage,
			Setting<Boolean> shouldFailover){
		this.mainStorage = mainStorage;
		this.standbyStorage = standbyStorage;
		this.shouldFailover = shouldFailover;
	}

	@Override
	public void put(D databean, Config config){
		if(shouldFailover.getValue()){
			standbyStorage.put(databean, config);
		}else{
			mainStorage.put(databean, config);
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		if(shouldFailover.getValue()){
			standbyStorage.putMulti(databeans, config);
		}else{
			mainStorage.putMulti(databeans, config);
		}
	}

}

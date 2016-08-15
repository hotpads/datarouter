package com.hotpads.datarouter.storage.failover;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Singleton;

import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class FailoverRecoveryService{

	private final List<RecoveryPolicy<?,?>> recoveryPolicies;

	public FailoverRecoveryService(){
		this.recoveryPolicies = new ArrayList<>();
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> void registerRecoveryPolicy(
			StorageWriter<PK,D> mainStorage, Supplier<D> databeanPoller, Setting<Boolean> isFailedOver){
		recoveryPolicies.add(new RecoveryPolicy<>(mainStorage, databeanPoller, isFailedOver));
	}

	public void runRecovery(){
		recoveryPolicies.forEach(RecoveryPolicy::recover);
	}

	private class RecoveryPolicy<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

		private final StorageWriter<PK,D> mainStorage;
		private final Supplier<D> databeanPoller;
		private final Setting<Boolean> isFailedOver;

		private RecoveryPolicy(StorageWriter<PK,D> mainStorage, Supplier<D> databeanPoller,
				Setting<Boolean> isFailedOver){
			this.mainStorage = mainStorage;
			this.databeanPoller = databeanPoller;
			this.isFailedOver = isFailedOver;
		}

		private void recover(){
			if(isFailedOver.getValue()){
				return;
			}
			D databean;
			while((databean = databeanPoller.get()) != null){
				mainStorage.put(databean, null);
			}
		}

	}

}

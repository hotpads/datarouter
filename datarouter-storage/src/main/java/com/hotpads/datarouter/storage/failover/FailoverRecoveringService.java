package com.hotpads.datarouter.storage.failover;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Singleton;

import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class FailoverRecoveringService{

	private final List<RecoveringPolicy<?,?>> recoveringPolicies;

	public FailoverRecoveringService(){
		this.recoveringPolicies = new ArrayList<>();
	}

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> void registerRecoveryPolicy(
			StorageWriter<PK,D> mainStorage, Supplier<D> databeanPoller){
		recoveringPolicies.add(new RecoveringPolicy<>(mainStorage, databeanPoller));
	}

	public void runRecovery(){
		recoveringPolicies.forEach(RecoveringPolicy::recover);
	}

	private class RecoveringPolicy<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

		private final StorageWriter<PK,D> mainStorage;
		private final Supplier<D> databeanPoller;

		private RecoveringPolicy(StorageWriter<PK,D> mainStorage, Supplier<D> databeanPoller){
			this.mainStorage = mainStorage;
			this.databeanPoller = databeanPoller;
		}

		private void recover(){
			D databean = databeanPoller.get();
			while(databean != null){
				mainStorage.put(databean, null);
				databean = databeanPoller.get();
			}
		}

	}

}

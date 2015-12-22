package com.hotpads.datarouter.storage.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class QueueMessage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends BaseQueueMessage<PK,D>{
	
	private D databean;
	
	public QueueMessage(byte[] handle, D databean){
		super(handle);
		this.databean = databean;
	}
	
	public D getDatabean(){
		return databean;
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<D> getDatabeans(
			Collection<QueueMessage<PK,D>> messages){
		List<D> databeans = new ArrayList<>();
		for(QueueMessage<PK,D> message : messages){
			databeans.add(message.getDatabean());
		}
		return databeans;
	}
}

package com.hotpads.datarouter.storage.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class BaseQueueMessage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	private QueueMessageKey key;
	
	public BaseQueueMessage(byte[] handle){
		this.key = new QueueMessageKey(handle);
	}
	
	public QueueMessageKey getKey(){
		return key;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<QueueMessageKey> getKeys(
			Collection<? extends BaseQueueMessage<PK,D>> messages){
		List<QueueMessageKey> keys = new ArrayList<>();
		for(BaseQueueMessage<PK,D> message : messages){
			keys.add(message.getKey());
		}
		return keys;
	}
}

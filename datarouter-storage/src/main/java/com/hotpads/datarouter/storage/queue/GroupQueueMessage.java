package com.hotpads.datarouter.storage.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class GroupQueueMessage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends BaseQueueMessage<PK,D>{

	private List<D> databeans;

	public GroupQueueMessage(byte[] handle, List<D> databeans){
		super(handle);
		this.databeans = databeans;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<D> getDatabeans(
			Collection<GroupQueueMessage<PK,D>> messages){
		List<D> databeans = new ArrayList<>();
		for(GroupQueueMessage<PK,D> message : messages){
			databeans.addAll(message.getDatabeans());
		}
		return databeans;
	}

	public List<D> getDatabeans(){
		return databeans;
	}

	public static boolean isEmpty(GroupQueueMessage<?,?> message){
		return message == null || message.isEmpty();
	}

	public boolean isEmpty(){
		return DrCollectionTool.isEmpty(databeans);
	}

	public boolean notEmpty(){
		return !isEmpty();
	}
}

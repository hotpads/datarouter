package com.hotpads.notification.request;

import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;
import com.hotpads.datarouter.util.core.DrHashMethods;

public class NotificationRequestPartitioner16 extends BaseEntityPartitioner<NotificationRequestEntityKey>{

	@Override
	public int getNumPartitions(){
		return 16;
	}

	@Override
	public int getPartition(NotificationRequestEntityKey ek){
		String hashInput = ek.getUserId().getType().getPersistentString() + ek.getUserId().getId();
		long hash = DrHashMethods.longDjbHash(hashInput);
		return (int)(hash % getNumPartitions());
	}

}
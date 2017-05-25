package com.hotpads.notification.itemlog;

import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;
import com.hotpads.datarouter.util.core.DrHashMethods;

public class NotificationItemLogPartitioner16 extends BaseEntityPartitioner<NotificationItemLogEntityKey>{

	@Override
	public int getNumPartitions(){
		return 16;
	}

	@Override
	public int getPartition(NotificationItemLogEntityKey ek){
		String hashInput = ek.getUserId().getType().getPersistentString() + ek.getUserId().getId();
		long hash = DrHashMethods.longDjbHash(hashInput);
		return (int)(hash % getNumPartitions());
	}

}
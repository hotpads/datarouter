package com.hotpads.notification.log;

import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;
import com.hotpads.datarouter.util.core.DrHashMethods;

public class NotificationLogPartitioner16 extends BaseEntityPartitioner<NotificationLogEntityKey>{

	@Override
	public int getNumPartitions() {
		return 16;
	}

	@Override
	public int getPartition(NotificationLogEntityKey ek){
		String hashInput = ek.getUserId().getType().getPersistentString() + ek.getUserId().getId();
		long hash = DrHashMethods.longDJBHash(hashInput);
		return (int)(hash % getNumPartitions());
	}

}
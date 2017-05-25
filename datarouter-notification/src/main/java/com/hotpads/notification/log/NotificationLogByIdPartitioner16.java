package com.hotpads.notification.log;

import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;
import com.hotpads.datarouter.util.core.DrHashMethods;

public class NotificationLogByIdPartitioner16 extends BaseEntityPartitioner<NotificationLogByIdEntityKey>{

	@Override
	public int getNumPartitions(){
		return 16;
	}

	@Override
	public int getPartition(NotificationLogByIdEntityKey ek){
		String hashInput = ek.getId();
		long hash = DrHashMethods.longDjbHash(hashInput);
		return (int)(hash % getNumPartitions());
	}

}

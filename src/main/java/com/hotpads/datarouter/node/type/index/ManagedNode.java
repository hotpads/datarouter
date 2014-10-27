package com.hotpads.datarouter.node.type.index;

import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface ManagedNode
	<IK extends PrimaryKey<IK>,
	IE extends Databean<IK, IE>,
	IF extends DatabeanFielder<IK, IE>>{

	String getName();
	DatabeanFieldInfo<IK, IE, IF> getFieldInfo();
}
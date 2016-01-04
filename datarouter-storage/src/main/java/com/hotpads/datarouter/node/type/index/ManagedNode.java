package com.hotpads.datarouter.node.type.index;

import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public interface ManagedNode
	<PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>,
	IK extends PrimaryKey<IK>,
	IE extends IndexEntry<IK,IE,PK,D>,
	IF extends DatabeanFielder<IK,IE>>{

	String getName();
	DatabeanFieldInfo<IK, IE, IF> getFieldInfo();
}
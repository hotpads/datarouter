package com.hotpads.datarouter.node.type.index.base;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public abstract class BaseManagedNode
		<PK extends PrimaryKey<PK>, 
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>> 
implements ManagedNode<IK, IE, IF>{
	
	private String name;
	protected DatabeanFieldInfo<IK, IE, IF> fieldInfo;
	protected PhysicalMapStorageNode<PK, D> node;

	public BaseManagedNode(PhysicalMapStorageNode<PK, D> node, NodeParams<IK, IE, IF> params, String name){
		this.node = node;
		this.name = name;
		this.fieldInfo = new DatabeanFieldInfo<IK, IE, IF>(name, params);
	}

	@Override
	public String getName(){
		return name;
	}
	
	public DatabeanFieldInfo<IK, IE, IF> getFieldInfo(){
		return fieldInfo;
	}

}

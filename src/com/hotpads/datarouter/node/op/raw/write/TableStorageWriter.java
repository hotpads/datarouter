package com.hotpads.datarouter.node.op.raw.write;

import java.util.Collection;
import java.util.Map;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface TableStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends NodeOps<PK,D>
{
	void updateField(String field, Object newValue, Collection<? extends PK> keys);
	void updateFields(Map<String,Object> newValueByFieldName, Collection<? extends PK> keys);
	
//	void updateFieldWhere(String field, Object newValue, RestrictionSet restrictionSet);
//	void updateFieldsWhere(Map<String,Object> newValueByFieldName, RestrictionSet restrictionSet);
	
//	void deleteWhere(RestrictionSet restrictionSet);
	

	public interface TableStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends Node<PK,D>, SortedStorageWriter<PK,D>
	{
	}

	public interface PhysicalTableStorageWriterNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends PhysicalNode<PK,D>, TableStorageWriterNode<PK,D>
	{
	}
}

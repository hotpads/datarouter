package com.hotpads.datarouter.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface TableStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends NodeOps<PK,D>
{
	
//	Key<D> getKeysWhere(RestrictionSet restrictionSet);
//	D getWhere(RestrictionSet restrictionSet);

	Object getField(String fieldName, PK key);
	List<Object> getFields(List<String> fieldNames, PK key);

	List<Object> getField(String fieldName, Collection<? extends PK> keys);
	List<List<Object>> getFields(List<String> fieldNames, Collection<? extends PK> keys);

//	List<Object> getFieldWhere(String fieldName, RestrictionSet restrictionSet);
//	List<List<Object>> getFieldsWhere(List<String> fieldNames, RestrictionSet restrictionSet);
	
	

	public interface TableStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, SortedStorageReader<PK,D>
	{
	}
	
	public interface PhysicalTableStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, TableStorageReaderNode<PK,D>
	{
	}
}

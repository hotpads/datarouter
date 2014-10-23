package com.hotpads.datarouter.client.imp.jdbc.node.index;

import java.util.List;

import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseManagedNode
		<PK extends PrimaryKey<PK>, 
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>> 
implements ManagedNode{
	
	protected Class<IF> indexFielderClass;
	protected Class<IE> indexEntryClass;

	public BaseManagedNode(Class<IE> indexEntryClass, Class<IF> indexFielderClass){
		this.indexFielderClass = indexFielderClass;
		this.indexEntryClass = indexEntryClass;
	}

	@Override
	public String getName(){
		return getClass().getSimpleName();
	}
	
	@Override
	public List<Field<?>> getFields(){
		IE sampleDatabean = ReflectionTool.create(indexEntryClass);
		return ReflectionTool.create(indexFielderClass).getFields(sampleDatabean);
	}

}

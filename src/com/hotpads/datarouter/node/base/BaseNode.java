package com.hotpads.datarouter.node.base;

import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.MapTool;

public abstract class BaseNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
implements Node<PK,D>{

	protected Class<PK> primaryKeyClass;
	protected Class<D> databeanClass;
	protected String name;
	protected List<Field<?>> primaryKeyFields;
	
	
	
	public BaseNode(Class<D> databeanClass){
		this.databeanClass = databeanClass;
		this.primaryKeyClass = DatabeanTool.getPrimaryKeyClass(databeanClass);
		this.name = databeanClass.getSimpleName()+"."+this.getClass().getSimpleName();//probably never used
		this.primaryKeyFields = FieldTool.getFieldsUsingReflection(primaryKeyClass);
	}

	
	@Override
	public Class<D> getDatabeanType() {
		return this.databeanClass;
	}

	@Override
	public String getName() {
		return this.name;
	}


	@Override
	public Map<PK,D> getByKey(Iterable<D> databeans){
		Map<PK,D> map = MapTool.createHashMap();
		for(D databean : IterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}
	
	
}

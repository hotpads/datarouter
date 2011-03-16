package com.hotpads.datarouter.serialize.fielder;

import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseDatabeanFielder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseFielder<D>
implements DatabeanFielder<PK,D>{
	
	protected BaseDatabeanFielder(){
		this.primaryKeyFielder = ReflectionTool.create(getKeyFielderClass());
	}

//	protected PKF primaryKeyFielder;
	protected Fielder<PK> primaryKeyFielder;
	
	
	@Override
	public Fielder<PK> getKeyFielder(){
		return primaryKeyFielder;
	}
	
	@Override
	public List<Field<?>> getKeyFields(D d){
		return FieldTool.setPrefixes(d.getKeyFieldName(), 
				primaryKeyFielder.getFields(d.getKey()));
	}
	
	@Override
	public List<Field<?>> getFields(D d){
		List<Field<?>> allFields = FieldTool.setPrefixes(
				d.getKeyFieldName(), getKeyFields(d));
		ListTool.nullSafeArrayAddAll(allFields, getNonKeyFields(d));
		return allFields;
	}
	
}

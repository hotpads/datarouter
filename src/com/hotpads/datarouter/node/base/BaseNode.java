package com.hotpads.datarouter.node.base;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
implements Node<PK,D>{
	protected Logger logger = Logger.getLogger(getClass());

	protected Class<PK> primaryKeyClass;
	protected PK samplePrimaryKey;
	protected Class<? super D> baseDatabeanClass;
	protected Class<D> databeanClass;
	protected D sampleDatabean;
	protected String name;

	protected boolean fieldAware;
	protected List<Field<?>> primaryKeyFields;
	protected List<Field<?>> fields;
	protected List<Field<?>> nonKeyFields;
	protected Map<String,Field<?>> fieldByMicroName;
	
	public BaseNode(Class<D> databeanClass){
		this.baseDatabeanClass = databeanClass;
		this.databeanClass = databeanClass;
		this.sampleDatabean = ReflectionTool.create(databeanClass);
		this.primaryKeyClass = this.sampleDatabean.getKeyClass();
		this.samplePrimaryKey = ReflectionTool.create(primaryKeyClass);
		this.name = databeanClass.getSimpleName()+"."+this.getClass().getSimpleName();//probably never used
		this.primaryKeyFields = this.samplePrimaryKey.getFields();
		try{
			this.fields = this.sampleDatabean.getFields();
			this.nonKeyFields = this.sampleDatabean.getNonKeyFields();
			this.fieldByMicroName = MapTool.createHashMap();
			for(Field<?> field : this.fields){
				this.fieldByMicroName.put(field.getName(), field);
			}
		}catch(Exception e){
		}
		this.fieldAware = CollectionTool.notEmpty(this.nonKeyFields);
		if(this.fieldAware){
			logger.warn("Found fieldAware Databean:"+this.databeanClass.getSimpleName());
		}
	}
	
	@Override
	public Class<D> getDatabeanType() {
		return this.databeanClass;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
}

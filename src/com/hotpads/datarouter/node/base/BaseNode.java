package com.hotpads.datarouter.node.base;

import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
implements Node<PK,D>{
	protected Logger logger = Logger.getLogger(getClass());

	protected Class<PK> primaryKeyClass;
	protected Class<? super D> baseDatabeanClass;
	protected Class<D> databeanClass;
	protected D dummyDatabean;
	protected String name;

	protected boolean fieldAware;
	protected List<Field<?>> primaryKeyFields;
	protected List<Field<?>> fields;
	protected List<Field<?>> nonKeyFields;
	
	public BaseNode(Class<D> databeanClass){
		this.baseDatabeanClass = databeanClass;
		this.databeanClass = databeanClass;
		this.dummyDatabean = ReflectionTool.create(databeanClass);
		this.primaryKeyClass = this.dummyDatabean.getKeyClass();
		this.name = databeanClass.getSimpleName()+"."+this.getClass().getSimpleName();//probably never used
		this.primaryKeyFields = this.dummyDatabean.getKeyFields();
		try{
			this.fields = this.dummyDatabean.getFields();
			this.nonKeyFields = ListTool.createArrayList(this.fields);
			this.nonKeyFields = this.dummyDatabean.getNonKeyFields();
			fieldAware = true;
			logger.warn("Found fieldAware Databean:"+this.databeanClass.getSimpleName());
		}catch(NotImplementedException nie){
			fieldAware = false;
		}catch(DataAccessException dae){
			fieldAware = false;
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

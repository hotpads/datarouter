package com.hotpads.datarouter.node;

import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ComparableTool;

public abstract class BaseNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
implements Node<PK,D>{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected DataRouterContext drContext;
	protected String name;
	protected DatabeanFieldInfo<PK,D,F> fieldInfo;
	
	public BaseNode(Class<D> databeanClass){
		this(databeanClass, null);
	}
	
	public BaseNode(Class<D> databeanClass, Class<F> fielderClass){
		this.name = databeanClass.getSimpleName() + "." + getClass().getSimpleName();// probably never used
		try{
			this.fieldInfo = new DatabeanFieldInfo<PK,D,F>(name, databeanClass, fielderClass);
		}catch(Exception probablyNoPkInstantiated){
			throw new IllegalArgumentException("could not instantiate "+name, probablyNoPkInstantiated);
		}
	}
	
	@Override
	public void setDataRouterContext(DataRouterContext drContext){
		this.drContext = drContext;
	}

	@Override
	public Class<PK> getPrimaryKeyType(){
		return this.fieldInfo.getPrimaryKeyClass();
	}
	
	@Override
	public Class<D> getDatabeanType() {
		return this.fieldInfo.getDatabeanClass();
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public List<Field<?>> getFields(){
		return this.fieldInfo.getFields();
	}
	
	@Override
	public int compareTo(Node<PK,D> o){
		return ComparableTool.nullFirstCompareTo(getName(), o.getName());
	}
	
	@Override
	public List<Field<?>> getNonKeyFields(D d){
		return fieldInfo.getNonKeyFields(d);
	}
	
	@Override
	public DatabeanFieldInfo<PK,D,?> getFieldInfo(){
		return fieldInfo;
	}
	
	
}

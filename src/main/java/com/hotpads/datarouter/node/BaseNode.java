package com.hotpads.datarouter.node;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.routing.DataRouter;
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
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private DataRouterContext drContext;
	private DataRouter router;
	private NodeId<PK,D,F> id;
	protected DatabeanFieldInfo<PK,D,F> fieldInfo;
	
	
	/*************** construct *********************/
	
	public BaseNode(NodeParams<PK,D,F> params){
		this.drContext = params.getRouter().getContext();
		this.router = params.getRouter();
		try{
			this.fieldInfo = new DatabeanFieldInfo<PK,D,F>(getName(), params);
		}catch(Exception probablyNoPkInstantiated){
			throw new IllegalArgumentException("could not instantiate "+getName()+" Check that the primary key is " +
					"instantiated in the databean constructor.", probablyNoPkInstantiated);
		}
		//this default id is frequently overridden
		this.setId(new NodeId<PK,D,F>((Class<Node<PK,D>>)getClass(), params, fieldInfo.getExplicitNodeName()));
	}
	
	@Override
	public DataRouterContext getDataRouterContext(){
		return drContext;
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
		return id==null ? null : id.getName();
	}
	
	protected void setId(NodeId<PK,D,F> id){
//		logger.warn("setId:"+id.getName());
		this.id = id;
	}
	
	public NodeId<PK,D,F> getId(){
		return id;
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
		return fieldInfo.getNonKeyFieldsWithValues(d);
	}
	
	@Override
	public DatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return fieldInfo;
	}
	
	@Override
	public DataRouter getRouter(){
		return router;
	}
	
}

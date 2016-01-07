package com.hotpads.datarouter.node;

import java.util.List;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrComparableTool;

public abstract class BaseNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Node<PK,D>{

	private Datarouter datarouter;
	private Router router;
	private NodeId<PK,D,F> id;
	protected DatabeanFieldInfo<PK,D,F> fieldInfo;


	/*************** construct *********************/

	public BaseNode(NodeParams<PK,D,F> params){
		this.datarouter = params.getRouter().getContext();
		this.router = params.getRouter();
		try{
			this.fieldInfo = new DatabeanFieldInfo<>(getName(), params);
		}catch(Exception probablyNoPkInstantiated){
			throw new IllegalArgumentException("could not instantiate " + getName() + " Check that the primary key is "
					+ "instantiated in the databean constructor.", probablyNoPkInstantiated);
		}
		//this default id is frequently overridden
		this.setId(new NodeId<>(getClass().getSimpleName(), params, fieldInfo.getExplicitNodeName()));
	}

	@Override
	public Datarouter getDatarouter(){
		return datarouter;
	}

	@Override
	public Class<PK> getPrimaryKeyType(){
		return this.fieldInfo.getPrimaryKeyClass();
	}

	@Override
	public boolean isPhysicalNodeOrWrapper(){
		return false;
	}

	@Override
	public PhysicalNode<PK,D> getPhysicalNodeIfApplicable(){
		return null;//let actual PhysicalNodes override this
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
	public int compareTo(Node<PK,D> other){
		return DrComparableTool.nullFirstCompareTo(getName(), other.getName());
	}

	@Override
	public List<Field<?>> getNonKeyFields(D databean){
		return fieldInfo.getNonKeyFieldsWithValues(databean);
	}

	@Override
	public DatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return fieldInfo;
	}

	@Override
	public Router getRouter(){
		return router;
	}

}

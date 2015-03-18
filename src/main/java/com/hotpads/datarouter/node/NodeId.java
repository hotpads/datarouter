package com.hotpads.datarouter.node;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrStringTool;

public class NodeId<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private Class<? extends Node<PK,D>> nodeClass;
	private NodeParams<PK,D,F> nodeParams;
	private Class<D> databeanClass;
	private String routerName;
	private String clientName;
	private String parentNodeName;
	private String explicitName;
	

	public NodeId(Class<? extends Node<PK,D>> nodeClass, NodeParams<PK,D,F> nodeParams, String explicitName){
		this(nodeClass, nodeParams.getDatabeanClass(), nodeParams.getRouter().getName(), 
				nodeParams.getClientName(), nodeParams.getParentName(), explicitName);
		this.nodeParams = nodeParams;
	}
	
	public NodeId(Class<? extends Node<PK,D>> nodeClass, Class<D> databeanClass, String routerName, String clientName, 
			String parentNodeName, String explicitName){
		this.nodeClass = Preconditions.checkNotNull(nodeClass);
		this.databeanClass = Preconditions.checkNotNull(databeanClass);
		this.routerName = Preconditions.checkNotNull(routerName);
		this.clientName = clientName;
		this.parentNodeName = parentNodeName;
		this.explicitName = explicitName;
	}
	
	public String getName(){
		//if name is specified
		if(DrStringTool.notEmpty(explicitName)){
			return explicitName;
		}
		
		//example: TraceEntity.TraceSpan.TS
//		if(nodeParams != null && StringTool.notEmpty(nodeParams.getEntityNodeName())){
//			return nodeParams.getEntityNodeName()+"."+databeanClass.getSimpleName()+"."+nodeParams.getEntityNodePrefix();
//		}
		
		//for PhysicalNodes that have a specific client.  this can distinguish a databean class between many masters,
		// slaves, partitions, etc
		if(DrStringTool.notEmpty(clientName)){
			String parentPrefix = DrStringTool.isEmpty(parentNodeName) ? "" : parentNodeName + ".";
			return parentPrefix+clientName+"."+databeanClass.getSimpleName();
		}
		
		//default case where there is no clientName (like MasterSlaveNode)
		return databeanClass.getSimpleName()+"."+nodeClass.getSimpleName();
	}
}

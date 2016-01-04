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

	private String nodeClassSimpleName;
	private String databeanClassName;
	private String clientName;
	private String parentNodeName;
	private String explicitName;


	public NodeId(String nodeClassSimpleName, NodeParams<PK,D,F> nodeParams, String explicitName){
		this(nodeClassSimpleName, nodeParams.getDatabeanName(), nodeParams.getRouter().getName(),
				nodeParams.getClientName(), nodeParams.getParentName(), explicitName);
	}

	public NodeId(String nodeClassSimpleName, String databeanClassName, String routerName, String clientName,
			String parentNodeName, String explicitName){
		this.nodeClassSimpleName = Preconditions.checkNotNull(nodeClassSimpleName);
		this.databeanClassName = databeanClassName;
		Preconditions.checkNotNull(routerName);
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
//		return nodeParams.getEntityNodeName()+"."+databeanClass.getSimpleName()+"."+nodeParams.getEntityNodePrefix();
//		}

		//for PhysicalNodes that have a specific client.  this can distinguish a databean class between many masters,
		// slaves, partitions, etc
		if(DrStringTool.notEmpty(clientName)){
			String parentPrefix = DrStringTool.isEmpty(parentNodeName) ? "" : parentNodeName + ".";
			return parentPrefix+clientName+"." + databeanClassName;
		}

		//default case where there is no clientName (like MasterSlaveNode)
		return databeanClassName + "."+nodeClassSimpleName;
	}
}

package com.hotpads.datarouter.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrStringTool;

public class NodeId<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(NodeId.class);

	private final String nodeClassSimpleName;
	private final String databeanClassName;
	private final String clientName;
	private final String parentNodeName;
	private final String explicitName;

	private final String name;


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

		this.name = calculateName();
	}


	private String calculateName(){
		String source;
		String name;
		if(DrStringTool.notEmpty(explicitName)){
			source = "explicitName";
			name = explicitName;
		}else if(DrStringTool.notEmpty(clientName)){
			source = "client";
			String parentPrefix = DrStringTool.isEmpty(parentNodeName) ? "" : parentNodeName + ".";
			name = parentPrefix + clientName + "." + databeanClassName;
		}else{
			source = "virtual";
			name = databeanClassName + "." + nodeClassSimpleName;
		}
		logger.warn("source={}, name={}", source, name);
		return name;
	}

	public String getName(){
		return name;
	}
}

package com.hotpads.datarouter.node;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.StringTool;

public class NodeId<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private Class<Node<PK,D>> nodeClass;
	private Class<D> databeanClass;
	private String routerName;
	private String clientName;
	private String parentNodeName;
	private String explicitName;
	
	
	public NodeId(Class<Node<PK,D>> nodeClass, Class<D> databeanClass, String routerName, String clientName, 
			String parentNodeName, String explicitName){
		this.nodeClass = nodeClass;
		this.databeanClass = databeanClass;
		this.routerName = Preconditions.checkNotNull(routerName);
		this.clientName = clientName;
		this.parentNodeName = parentNodeName;
		this.explicitName = explicitName;
	}
	
	public String getName(){
		//if name is specified
		if(StringTool.notEmpty(explicitName)){
			return explicitName;
		}
		
		//for PhysicalNodes that have a specific client.  this can distinguish a databean class between many masters,
		// slaves, partitions, etc
		if(StringTool.notEmpty(clientName)){
			return clientName+"."+databeanClass.getSimpleName();
		}
		
		//default case where there is no clientName
		return databeanClass.getSimpleName()+"."+nodeClass.getSimpleName();
	}
}

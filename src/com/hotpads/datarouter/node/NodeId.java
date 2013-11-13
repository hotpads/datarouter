package com.hotpads.datarouter.node;

import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class NodeId<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private Class<? extends Node<PK,D>> nodeClass;
	private Class<D> databeanClass;
	private String routerName;
	private String clientName;
	private String parentNodeName;
	private String nodeName;
	
	
	public NodeId(Class<? extends Node<PK,D>> nodeClass, Class<D> databeanClass, String routerName, String clientName, 
			String parentNodeName, String nodeName){
		this.nodeClass = nodeClass;
		this.databeanClass = databeanClass;
		this.routerName = routerName;
		this.clientName = clientName;
		this.parentNodeName = parentNodeName;
		this.nodeName = nodeName;
	}
	
	public String getName(){
		return databeanClass.getSimpleName()+"."+nodeName;
	}
}

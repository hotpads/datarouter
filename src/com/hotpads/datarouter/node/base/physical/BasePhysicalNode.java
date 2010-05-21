package com.hotpads.datarouter.node.base.physical;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.SetTool;

public abstract class BasePhysicalNode<D extends Databean> 
implements Node<D>, PhysicalNode<D>{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected Class<D> databeanClass;
	protected String clientName;
	protected String name;
	protected String physicalName;
	protected String packagedPhysicalName;
	
	protected DataRouter router;

	
	/****************************** constructors ********************************/
	
	public BasePhysicalNode(Class<D> databeanClass, DataRouter router, String clientName, 
			String physicalName, String packagedPhysicalName){
		this.databeanClass = databeanClass;
		this.clientName = clientName;
		this.name = clientName+"."+physicalName;
		this.router = router;
		this.physicalName = physicalName;
		this.packagedPhysicalName = packagedPhysicalName;
	}
	
	public BasePhysicalNode(Class<D> databeanClass, DataRouter router, String clientName){
		this(databeanClass, router, clientName, 
				databeanClass.getSimpleName(), databeanClass.getName());
	}
	

	/****************************** node methods ********************************/

	@Override
	public Class<D> getDatabeanType() {
		return this.databeanClass;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public <K extends UniqueKey<D>> List<String> getClientNamesForKeys(Collection<K> keys) {
		return ListTool.createLinkedList(this.clientName);
	}

	/********************** physical node methods *********************************/
	
	public String getClientName() {
		return clientName;
	}

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = SetTool.createTreeSet();
		clientNames.add(this.clientName);
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName) {
		return ObjectTool.nullSafeEquals(this.clientName, clientName);
	}
		
	@Override
	public List<? extends PhysicalNode<D>> getPhysicalNodes() {
		List<PhysicalNode<D>> physicalNodes = ListTool.createLinkedList();
		physicalNodes.add(this);
		return physicalNodes;
	}

	@Override
	public List<PhysicalNode<D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<D>> physicalNodes = ListTool.createLinkedList();
		if(clientName.equals(this.clientName)){
			physicalNodes.add(this);
		}
		return physicalNodes;
	}

	@Override
	public String getPhysicalName() {
		return this.physicalName;
	}
	
	@Override
	public String getPackagedPhysicalName(){
		return this.packagedPhysicalName;
	}
	
	@Override
	public String toString(){
		return this.getName();
	}

//	public abstract Client getClient();
	
}

package com.hotpads.datarouter.node.base.physical;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.base.BaseNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.SetTool;

public abstract class BasePhysicalNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends BaseNode<PK,D>
implements PhysicalNode<PK,D>
{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected String clientName;
	protected String physicalName;
	protected String packagedPhysicalName;
	
	protected DataRouter router;

	
	/****************************** constructors ********************************/
	
	public BasePhysicalNode(Class<D> databeanClass, 
			DataRouter router, String clientName){
		super(databeanClass);
		this.router = router;
		this.clientName = clientName;
		this.physicalName = databeanClass.getSimpleName();
		this.packagedPhysicalName = databeanClass.getName();
		this.name = clientName+"."+databeanClass.getSimpleName();//watch out for name collisions on subclasses, etc..
	}
	
	public BasePhysicalNode(Class<D> databeanClass,
			DataRouter router, String clientName, 
			String physicalName, String packagedPhysicalName){
		this(databeanClass, router, clientName);
		//overwrite the default values
		this.physicalName = physicalName;
		this.packagedPhysicalName = packagedPhysicalName;
	}
	

	/****************************** node methods ********************************/
	
	@Override
	public <K extends UniqueKey<PK>> List<String> getClientNamesForKeys(Collection<K> keys) {
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
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodes() {
		List<PhysicalNode<PK,D>> physicalNodes = ListTool.createLinkedList();
		physicalNodes.add(this);
		return physicalNodes;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> physicalNodes = ListTool.createLinkedList();
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

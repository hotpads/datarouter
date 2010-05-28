package com.hotpads.datarouter.node.base.physical;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.SetTool;

public abstract class BasePhysicalNode<D extends Databean,PK extends PrimaryKey<D>> 
implements Node<D,PK>, PhysicalNode<D,PK>{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected Class<D> databeanClass;
	protected Class<PK> primaryKeyClass;
	protected List<Field<?>> primaryKeyFields;
	protected String clientName;
	protected String name;
	protected String physicalName;
	protected String packagedPhysicalName;
	
	protected DataRouter router;

	
	/****************************** constructors ********************************/
	
	public BasePhysicalNode(Class<D> databeanClass, Class<PK> primaryKeyClass, 
			DataRouter router, String clientName){
		this.databeanClass = databeanClass;
		this.primaryKeyClass = primaryKeyClass;
		this.clientName = clientName;
		this.router = router;
		this.primaryKeyFields = FieldTool.getFieldsUsingReflection(primaryKeyClass);
		this.physicalName = databeanClass.getSimpleName();
		this.packagedPhysicalName = databeanClass.getName();
		this.name = clientName+"."+physicalName;
	}
	
	public BasePhysicalNode(Class<PK> primaryKeyClass, 
			DataRouter router, String clientName){
		this.databeanClass = KeyTool.getDatabeanClass(primaryKeyClass);
		this.primaryKeyClass = primaryKeyClass;
		this.clientName = clientName;
		this.router = router;
		this.primaryKeyFields = FieldTool.getFieldsUsingReflection(primaryKeyClass);
		this.physicalName = databeanClass.getSimpleName();
		this.packagedPhysicalName = databeanClass.getName();
		this.name = clientName+"."+physicalName;
	}
	
	public BasePhysicalNode(Class<PK> primaryKeyClass, 
			DataRouter router, String clientName, 
			String physicalName, String packagedPhysicalName){
		this(primaryKeyClass, router, clientName);
		//overwrite the default values
		this.physicalName = physicalName;
		this.packagedPhysicalName = packagedPhysicalName;
		this.name = clientName+"."+physicalName;
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
	public List<? extends PhysicalNode<D,PK>> getPhysicalNodes() {
		List<PhysicalNode<D,PK>> physicalNodes = ListTool.createLinkedList();
		physicalNodes.add(this);
		return physicalNodes;
	}

	@Override
	public List<PhysicalNode<D,PK>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<D,PK>> physicalNodes = ListTool.createLinkedList();
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

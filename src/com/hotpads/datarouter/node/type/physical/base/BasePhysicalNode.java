package com.hotpads.datarouter.node.type.physical.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import junit.framework.Assert;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeId;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.SetTool;

public abstract class BasePhysicalNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseNode<PK,D,F>
implements PhysicalNode<PK,D>
{
	protected Logger logger = Logger.getLogger(getClass());
	
	protected String clientName;
	protected String tableName;
	protected String packagedTableName;
	
	protected DataRouter router;

	
	/****************************** constructors ********************************/
	
	public BasePhysicalNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName){
		super(router.getContext(), databeanClass, fielderClass);
		this.router = router;
		this.clientName = clientName;
		this.tableName = databeanClass.getSimpleName();
		this.packagedTableName = databeanClass.getName();
		this.id = new NodeId<PK,D,F>((Class<Node<PK,D>>)getClass(), databeanClass, router.getName(), clientName, 
				null, null);
		this.name = clientName+"."+databeanClass.getSimpleName();
		Assert.assertEquals(name, id.getName());
//		if(this.fieldAware){
//			logger.warn("Found fieldAware Databean:"+this.getName());
//		}
	}
	
	//for things like the event.Event monthly partitioned tables
	public BasePhysicalNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String tableName, String packagedTableName){
		this(databeanClass, fielderClass, router, clientName);
		//overwrite the default values
		this.tableName = tableName;
		this.packagedTableName = packagedTableName;
		this.name = clientName+"."+tableName;
		logger.info("client:"+this.clientName+" databean "+databeanClass.getSimpleName()+" -> "+tableName);
	}
	
	//for table-per-class hierarchy like the property.Photo hierarchy
	public BasePhysicalNode(Class<? super D> baseDatabeanClass, 
			Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName){
		this(databeanClass, fielderClass, router, clientName);
		//overwrite the default values
		this.fieldInfo.setBaseDatabeanClass(baseDatabeanClass);
		this.tableName = baseDatabeanClass.getSimpleName();
		logger.info("client:"+this.clientName+" databean "+databeanClass.getSimpleName()+" -> "+tableName);
	}
	

	/****************************** node methods ********************************/
	
	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return ListTool.createLinkedList(this.clientName);
	}

	/********************** physical node methods *********************************/
	
	public String getClientName() {
		return clientName;
	}

	@Override
	public Set<String> getAllNames(){
		return SetTool.wrap(this.name);
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
	public Node<PK,D> getMaster() {
		return null;
	}
	
	@Override
	public List<Node<PK,D>> getChildNodes(){
		return ListTool.create();
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
	public String getTableName() {
		return this.tableName;
	}
	
	@Override
	public String getPackagedTableName(){
		return this.packagedTableName;
	}
	
	@Override
	public String toString(){
		return this.getName();
	}

//	public abstract Client getClient();

}

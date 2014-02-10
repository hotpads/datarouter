package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.java.ReflectionTool;

public class Nodes<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>>{
	static Logger logger = Logger.getLogger(Nodes.class);
	
	
	/************************ fields *******************************/
	
	protected DataRouterContext drContext;

	protected List<N> topLevelNodes = ListTool.createArrayList();
	protected List<N> allNodes = ListTool.createArrayList();
	protected List<String> allNames = ListTool.createArrayList();
	protected Map<String,N> nodeByName = MapTool.createTreeMap();
	protected SortedMap<String,SortedSet<N>> nodesByRouterName = MapTool.createTreeMap();
	protected Multimap<String,N> topLevelNodesByRouterName = TreeMultimap.create();
	protected Map<N,String> routerNameByNode = MapTool.createTreeMap();
//	protected Map<ClientType,List<N>> nodesByClientType = MapTool.createTreeMap();
	protected Map<String,Map<String,PhysicalNode<PK,D>>> physicalNodeByTableNameByClientName = MapTool.createTreeMap();
	protected Map<Class<PK>,N> nodeByPrimaryKeyType = MapTool.createHashMap();//won't this have collissions?
	protected Map<Class<D>,N> nodeByDatabeanType = MapTool.createHashMap();//won't this have collissions?
	protected Map<Class<D>,List<String>> clientNamesByDatabeanType = MapTool.createHashMap();
	
	
	/********************** constructors **********************************/
	
	public Nodes(DataRouterContext drContext){
		this.drContext = drContext;
	}
	
	
	/*********************** methods ************************************/
	
	public N register(String routerName, N node){
		logger.info("register:"+node.getName());
		ensureDuplicateNamesReferToSameNode(node);
		Class<D> databeanType = node.getDatabeanType();
		D sampleDatabean = ReflectionTool.create(databeanType);
		List<String> clientNames = node.getClientNames();
		@SuppressWarnings("unchecked")
		List<N> nodeWithDescendants = (List<N>)NodeTool.getNodeAndDescendants(node);
		this.topLevelNodes.add(node);
		this.topLevelNodesByRouterName.put(routerName, node);
		this.allNodes.addAll(nodeWithDescendants);
		for(N nodeOrDescendant : IterableTool.nullSafe(nodeWithDescendants)){
			allNames.add(nodeOrDescendant.getName());
			nodeByName.put(nodeOrDescendant.getName(), nodeOrDescendant);
			if(nodeOrDescendant instanceof PhysicalNode){
				PhysicalNode<PK,D> physicalNode = (PhysicalNode<PK,D>)nodeOrDescendant;
				String clientName = physicalNode.getClientName();
				String tableName = physicalNode.getTableName();
				if(physicalNodeByTableNameByClientName.get(clientName)==null){
					physicalNodeByTableNameByClientName.put(clientName, new TreeMap<String,PhysicalNode<PK,D>>());
				}
				physicalNodeByTableNameByClientName.get(clientName).put(tableName, physicalNode);
			}
			if(!nodesByRouterName.containsKey(routerName)) {
				nodesByRouterName.put(routerName, new TreeSet<N>());
			}
			nodesByRouterName.get(routerName).add(nodeOrDescendant);
			routerNameByNode.put(nodeOrDescendant, routerName);
		}
		nodeByPrimaryKeyType.put(sampleDatabean.getKeyClass(), node);
		nodeByDatabeanType.put(databeanType, node);
		if(clientNamesByDatabeanType.get(databeanType)==null){
			clientNamesByDatabeanType.put(databeanType, new LinkedList<String>());
		}
		clientNamesByDatabeanType.get(databeanType).addAll(clientNames);
		
		Collections.sort(topLevelNodes);
		Collections.sort(allNodes);
		Collections.sort(allNames);
		
		return node;
	}
	
	public N getNode(String nodeName){
		return nodeByName.get(nodeName);
	}
	
	public Set<Class<D>> getTypesForClient(String clientName){
		Set<Class<D>> types = SetTool.createHashSet();
		for(N node : MapTool.nullSafe(nodeByName).values()){
			if(node.usesClient(clientName)){
				types.add(node.getDatabeanType());
			}
		}
		return types;
	}
	
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName){
		List<PhysicalNode<PK,D>> physicalNodesForClient = ListTool.createLinkedList();
		for(N node : IterableTool.nullSafe(topLevelNodes)){
			List<? extends PhysicalNode<PK,D>> physicalNodesForNode = node.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<PK,D> physicalNode : CollectionTool.nullSafe(physicalNodesForNode)){
				physicalNodesForClient.add(physicalNode);
			}
		}
		Collections.sort(physicalNodesForClient);
		return physicalNodesForClient;
	}
	
	public List<String> getTableNamesForClient(String clientName){
		List<? extends PhysicalNode<PK,D>> physicalNodesForClient = getPhysicalNodesForClient(clientName);
		List<String> tableNames = ListTool.create();
		for(PhysicalNode<PK,D> physicalNode : IterableTool.nullSafe(physicalNodesForClient)){
			tableNames.add(physicalNode.getTableName());
		}
		return tableNames;
	}
	
	public List<String> getTableNamesForRouterAndClient(String routerName, String clientName){
		List<? extends PhysicalNode<PK,D>> physicalNodesForClient = getPhysicalNodesForClient(clientName);
		List<String> tableNames = ListTool.create();
		for(PhysicalNode<PK,D> physicalNode : IterableTool.nullSafe(physicalNodesForClient)){
			if(ObjectTool.equals(routerNameByNode.get(physicalNode), routerName)){
				tableNames.add(physicalNode.getTableName());
			}
		}
		return tableNames;
	}
	
	public SortedSet<N> getNodesForRouterName(String routerName){
		return nodesByRouterName.get(routerName);
	}
	
	public N getNode(Key<PK> key){
		return this.nodeByPrimaryKeyType.get(key.getClass());
	}
	
	public N getNode(D databean){
		return this.nodeByDatabeanType.get(databean.getClass());//should just call getNode
	}
	
	public List<String> getClientNamesForKeys(Collection<PK> keys){
		SortedSet<String> clientNames = SetTool.createTreeSet();
		Map<N,LinkedList<PK>> keysByNode = MapTool.createHashMap();
		for(PK key : CollectionTool.nullSafe(keys)){
			N node = this.getNode(key);
			if(keysByNode.get(node)==null){
				keysByNode.put(node, new LinkedList<PK>());
			}
			keysByNode.get(node).add(key);
		}
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<String> nodeClientNames = node.getClientNamesForPrimaryKeysForSchemaUpdate(keysByNode.get(node));
			clientNames.addAll(nodeClientNames);
		}
		return ListTool.createArrayList(clientNames);
	}

	public List<String> getClientNamesForDatabeanType(Class<D> databeanType){
		return clientNamesByDatabeanType.get(databeanType);
	}
	
	public void clearThreadSpecificState(){
		for(N node : MapTool.nullSafe(nodeByName).values()){
			node.clearThreadSpecificState();
		}
	}
	
	protected void ensureDuplicateNamesReferToSameNode(N node){
		String thisName = node.getName();
		N existingNode = nodeByName.get(thisName);
		if(existingNode == null || existingNode == node){ return; }
		Class<?> existingNodeClass = existingNode.getClass();
		throw new IllegalArgumentException("different node with this name already exists:"+thisName+"["
				+existingNodeClass.getSimpleName()+"]");
	}
	
	public PhysicalNode<PK,D> getPhyiscalNodeForClientAndTable(String clientName, String tableName){
		try{
			return physicalNodeByTableNameByClientName.get(clientName).get(tableName);
		}catch(NullPointerException e){
			return null;
		}
	}
	
	
	/*************************** get/set ****************************************/
	
	public List<String> getAllNames(){
		return allNames;
	}
	
	public List<N> getAllNodes(){
		return allNodes;
	}
	
	public List<N> getTopLevelNodes(){
		return topLevelNodes;
	}

	public Multimap<String,N> getTopLevelNodesByRouterName(){
		return topLevelNodesByRouterName;
	}
	
	
}

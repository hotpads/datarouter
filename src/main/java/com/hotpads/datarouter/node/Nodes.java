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

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrSetTool;
import com.hotpads.util.core.java.ReflectionTool;

/**
 * Nodes is a registry of all Nodes in a DatarouterContext. It ensures that no two nodes try to share the same name. It
 * can be used by Datarouter management features like a web page to browse all nodes in the system.
 * 
 * @author mcorgan
 * 
 * @param <PK>
 * @param <D>
 * @param <N>
 */
@Singleton
public class Nodes<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(Nodes.class);
	
	
	/************************ fields *******************************/
	
	private List<N> topLevelNodes;
	private List<N> allNodes;
	private List<String> allNames;
	private Map<String,N> nodeByName;
	private SortedMap<String,SortedSet<N>> nodesByRouterName;
	private Multimap<String,N> topLevelNodesByRouterName;
	private Map<N,String> routerNameByNode;
	private Map<String,Map<String,PhysicalNode<PK,D>>> physicalNodeByTableNameByClientName;
	private Map<Class<PK>,N> nodeByPrimaryKeyType;
	private Map<Class<D>,N> nodeByDatabeanType;
	private Map<Class<D>,List<String>> clientNamesByDatabeanType;
	
	
	/********************** constructors **********************************/

//	@Inject //spring doesn't like @Inject without params
	Nodes(){
		this.topLevelNodes = DrListTool.createArrayList();
		this.allNodes = DrListTool.createArrayList();
		this.allNames = DrListTool.createArrayList();
		this.nodeByName = DrMapTool.createTreeMap();
		this.nodesByRouterName = DrMapTool.createTreeMap();
		this.topLevelNodesByRouterName = TreeMultimap.create();
		this.routerNameByNode = DrMapTool.createTreeMap();
		this.physicalNodeByTableNameByClientName = DrMapTool.createTreeMap();
		this.nodeByPrimaryKeyType = DrMapTool.createHashMap();//won't this have collissions?
		this.nodeByDatabeanType = DrMapTool.createHashMap();//won't this have collissions?
		this.clientNamesByDatabeanType = DrMapTool.createHashMap();
	}
	
	
	/*********************** methods ************************************/
	
	public N register(String routerName, N node){
//		logger.warn("register:"+node.getName());
		ensureDuplicateNamesReferToSameNode(node);
		Class<D> databeanType = node.getDatabeanType();
		D sampleDatabean = ReflectionTool.create(databeanType);
		List<String> clientNames = node.getClientNames();
		@SuppressWarnings("unchecked")
		List<N> nodeWithDescendants = (List<N>)NodeTool.getNodeAndDescendants(node);
		this.topLevelNodes.add(node);
		this.topLevelNodesByRouterName.put(routerName, node);
		this.allNodes.addAll(nodeWithDescendants);
		for(N nodeOrDescendant : DrIterableTool.nullSafe(nodeWithDescendants)){
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
		Set<Class<D>> types = DrSetTool.createHashSet();
		for(N node : DrMapTool.nullSafe(nodeByName).values()){
			if(node.usesClient(clientName)){
				types.add(node.getDatabeanType());
			}
		}
		return types;
	}
	
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName){
		List<PhysicalNode<PK,D>> physicalNodesForClient = DrListTool.createLinkedList();
		for(N node : DrIterableTool.nullSafe(topLevelNodes)){
			List<? extends PhysicalNode<PK,D>> physicalNodesForNode = node.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<PK,D> physicalNode : DrCollectionTool.nullSafe(physicalNodesForNode)){
				physicalNodesForClient.add(physicalNode);
			}
		}
		Collections.sort(physicalNodesForClient);
		return physicalNodesForClient;
	}
	
	public List<String> getTableNamesForClient(String clientName){
		List<? extends PhysicalNode<PK,D>> physicalNodesForClient = getPhysicalNodesForClient(clientName);
		List<String> tableNames = DrListTool.create();
		for(PhysicalNode<PK,D> physicalNode : DrIterableTool.nullSafe(physicalNodesForClient)){
			tableNames.add(physicalNode.getTableName());
		}
		return tableNames;
	}
	
	public List<String> getTableNamesForRouterAndClient(String routerName, String clientName){
		List<? extends PhysicalNode<PK,D>> physicalNodesForClient = getPhysicalNodesForClient(clientName);
		List<String> tableNames = DrListTool.create();
		for(PhysicalNode<PK,D> physicalNode : DrIterableTool.nullSafe(physicalNodesForClient)){
			if(DrObjectTool.equals(routerNameByNode.get(physicalNode), routerName)){
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
		SortedSet<String> clientNames = DrSetTool.createTreeSet();
		Map<N,LinkedList<PK>> keysByNode = DrMapTool.createHashMap();
		for(PK key : DrCollectionTool.nullSafe(keys)){
			N node = this.getNode(key);
			if(keysByNode.get(node)==null){
				keysByNode.put(node, new LinkedList<PK>());
			}
			keysByNode.get(node).add(key);
		}
		for(N node : DrMapTool.nullSafe(keysByNode).keySet()){
			Collection<String> nodeClientNames = node.getClientNamesForPrimaryKeysForSchemaUpdate(keysByNode.get(node));
			clientNames.addAll(nodeClientNames);
		}
		return DrListTool.createArrayList(clientNames);
	}

	public List<String> getClientNamesForDatabeanType(Class<D> databeanType){
		return clientNamesByDatabeanType.get(databeanType);
	}
	
	private void ensureDuplicateNamesReferToSameNode(N node){
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

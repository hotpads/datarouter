package com.hotpads.datarouter.node.base.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.BaseNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public abstract class BaseCachingNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends Node<PK,D>> 
extends BaseNode<PK,D,DatabeanFielder<PK,D>>{
	
	
	/***************************** Node pass-through stuff **********************************/
	
	protected N backingNode;
	
	public BaseCachingNode(N backingNode){
		super(backingNode.getDatabeanType());
		this.backingNode = backingNode;
	}

	
	@Override
	public void clearThreadSpecificState(){
		this.clearMapCacheForThisThread();
		this.clearNonMapCaches();
	}

	/************************* cache access *******************************/

	public Map<String,Map<PK,D>> mapCacheByThreadName = MapTool.createHashMap();

	public Map<PK,D> getMapCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		if(mapCacheByThreadName.get(threadName)==null){
			mapCacheByThreadName.put(threadName, new HashMap<PK,D>());
		}
		return mapCacheByThreadName.get(threadName);
	}
	
	public void clearMapCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		this.mapCacheByThreadName.remove(threadName);
	}
	
	
	/************************* cache access *******************************/

	// lookupCache
	protected Map<String,Map<Lookup<PK>,List<D>>> lookupCacheByThreadName = MapTool.createHashMap();

	protected Map<Lookup<PK>,List<D>> getLookupCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		if(lookupCacheByThreadName.get(threadName)==null){
			lookupCacheByThreadName.put(threadName, new HashMap<Lookup<PK>,List<D>>());
		}
		return lookupCacheByThreadName.get(threadName);
	}
	
	protected void clearLookupCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		this.lookupCacheByThreadName.remove(threadName);
	}
	
	// firstRecordCache
	protected Map<String,D> firstRecordByThreadName = MapTool.createHashMap();

	protected D getFirstRecordCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		return this.firstRecordByThreadName.get(threadName);
	}

	protected void setFirstRecordCacheForThisThread(D firstRecord){
		String threadName = Thread.currentThread().getName();
		this.firstRecordByThreadName.put(threadName, firstRecord);
	}
	
	protected void clearFirstRecordCacheForThisThread(){
		String threadName = Thread.currentThread().getName();
		this.firstRecordByThreadName.remove(threadName);
	}
	
	public void clearNonMapCaches(){
		this.clearLookupCacheForThisThread();
		this.clearFirstRecordCacheForThisThread();
	}
	
	
	/**************************************************************************/
	
	@Override
	public List<String> getClientNames() {
		return this.backingNode.getClientNames();
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return this.backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
	}

	@Override
	public Class<D> getDatabeanType() {
		return this.backingNode.getDatabeanType();
	}

	@Override
	public Node<PK,D> getMaster() {
		return this.backingNode.getMaster();
	}
	
	@Override
	public List<? extends Node<PK,D>> getChildNodes(){
		if(backingNode==null){ return ListTool.create(); }
		return ListTool.wrap(backingNode);
	}

	@Override
	public String getName() {
		return this.backingNode.getName();
	}

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.wrap(this.name);
		names.addAll(this.backingNode.getAllNames());
		return names;
	}

	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodes() {
		return this.backingNode.getPhysicalNodes();
	}

	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		return this.backingNode.getPhysicalNodesForClient(clientName);
	}

	@Override
	public boolean usesClient(String clientName) {
		return this.backingNode.usesClient(clientName);
	}

	public N getBackingNode(){
		return backingNode;
	}

	
	
}

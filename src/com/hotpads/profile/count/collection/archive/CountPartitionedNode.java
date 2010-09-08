package com.hotpads.profile.count.collection.archive;

import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeFactory;
import com.hotpads.datarouter.node.type.partitioned.PartitionedSortedStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalSortedStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.StringTool;

public class CountPartitionedNode
extends PartitionedSortedStorageNode<CountKey,Count,PhysicalSortedStorageNode<CountKey,Count>>{

	public static final String table_prefix = Count.class.getSimpleName();
	public static final String entity_prefix = Count.class.getName();
	
	public static long 
		s = 1000,//ms in second
		m = 60*s,//ms in minute
		h = 60*m,
		d = 24*h;
	
	public static long getMs(String t){
		int n = Integer.valueOf(StringTool.retainDigits(t));
		String units = StringTool.retainLetters(t);
		if("s".equals(units)){ return n*s; }
		else if("m".equals(units)){ return n*m; }
		else if("h".equals(units)){ return n*h; }
		else if("d".equals(units)){ return n*d; }
		else throw new IllegalArgumentException("unknown duration:"+t);
	}
	
	public static String getSuffix(long periodMs){
		if(periodMs >= d){ return periodMs / d + "d"; }
		else if(periodMs >= h){ return periodMs / h + "h"; }
		else if(periodMs >= m){ return periodMs / m + "m"; }
		else if(periodMs >= s){ return periodMs / s + "s"; }
		else throw new IllegalArgumentException("unknown duration:"+periodMs);
	}
	
	public String getNodeName(long periodMs){
		return this.getName() + getSuffix(periodMs);
	}
	
	public static final List<String> suffixes =     ListTool.create("5s", "20s", "1m", "5m",   "20m", "1h", "4h", "1d");
	public static final List<String> flushPeriods = ListTool.create("5s", "10s", "20s", "30s", "1m",  "1m", "1m", "1m");
	
	public static Map<String,Long> msBySuffix = MapTool.createHashMap();
	public static Map<Long,String> suffixByMs = MapTool.createHashMap();
	public static Map<Long,Long> flushPeriodByPeriod = MapTool.createHashMap();
	static{
		for(int i=0; i < suffixes.size(); ++i){
			String suffix = suffixes.get(i);
			msBySuffix.put(suffix, getMs(suffix));
			suffixByMs.put(getMs(suffix), suffix);
			String flushPeriod = flushPeriods.get(i);
			flushPeriodByPeriod.put(getMs(suffix), getMs(flushPeriod));
		}
	}
	
	public static final Map<Long,Integer> indexByMs = MapTool.createHashMap();
	static{
		int index = -1;
		for(String suffix : suffixes){ indexByMs.put(getMs(suffix), ++index); }
	}
	
	
	/********************************* constructor *************************************/
	
	public CountPartitionedNode(DataRouter router, String clientName){
		super(Count.class, router);
		for(String suffix : suffixes){
			String tableName = table_prefix + suffix;
			String entityName = entity_prefix + suffix;
			Node<CountKey,Count> node = NodeFactory.create(
					clientName, tableName, entityName, Count.class, router);
			PhysicalSortedStorageNode<CountKey,Count> sortedNode = BaseDataRouter.cast(node);
			this.register(sortedNode);
		}
	}

	/********************************** required ************************************/
	
	@Override
	public boolean isPartitionAware(Key<CountKey> key){
		return knowsPartition(key);
	}
	
	public static boolean knowsPartition(Key<CountKey> key){
		//TODO don't assume it's the PK
		return ((CountKey)key).getPeriodMs()!=null;
	}
	
	@Override
	public List<PhysicalSortedStorageNode<CountKey,Count>> getPhysicalNodes(Key<CountKey> key) {
		if(!isPartitionAware(key)){ return this.getPhysicalNodes(); }
		CountKey countKey = (CountKey)key;
		Integer index = indexByMs.get(countKey.getPeriodMs());
		PhysicalSortedStorageNode<CountKey,Count> node = this.physicalNodes.get(index);
		if(node==null){ return ListTool.createLinkedList(); }
		return ListTool.wrap(node);
	}
}

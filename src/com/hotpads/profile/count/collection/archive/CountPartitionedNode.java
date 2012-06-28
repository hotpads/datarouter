package com.hotpads.profile.count.collection.archive;

import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.PartitionedSortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.Count.CountFielder;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.collections.Range;

public class CountPartitionedNode
extends PartitionedSortedMapStorageNode<CountKey,Count,CountFielder,PhysicalSortedMapStorageNode<CountKey,Count>>{

	public static final String TABLE_PREFIX = Count.class.getSimpleName();
	public static final String ENTITY_PREFIX = Count.class.getName();
	
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
		return getName() + getSuffix(periodMs);
	}
	
	//recommended Time-to-live's in the comments.  hbase ttl's are ints.  Integer.MAX_VALUE is 63 years
	public static final List<String> suffixes = ListTool.create(
			"5s",  //    2764800 - that is 552960 records - ttl of 32 days
			"20s", //   11059200 - same 552960 records
			"1m",  //   33177600 - etc
			"5m",  //  165888000
			"20m", //  663552000 - something like 15 years
			"1h",  //
			"4h",  // 
			"1d"); // no ttl
	
	public static final List<String> flushPeriods = ListTool.create("5s", "20s", "1m", "5m", "10m",  "30m", "1h", "1h");
	
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
		super(Count.class, CountFielder.class, router);
		for(String suffix : suffixes){
			String tableName = TABLE_PREFIX + suffix;
			String entityName = ENTITY_PREFIX + suffix;
			Node<CountKey,Count> node = NodeFactory.create(clientName, tableName, entityName, Count.class,
					CountFielder.class, router);
			PhysicalSortedMapStorageNode<CountKey,Count> sortedNode = BaseDataRouter.cast(node);
			register(sortedNode);
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
	public List<PhysicalSortedMapStorageNode<CountKey,Count>> getPhysicalNodes(Key<CountKey> key) {
		if(!isPartitionAware(key)){ return getPhysicalNodes(); }
		CountKey countKey = (CountKey)key;
		Integer index = indexByMs.get(countKey.getPeriodMs());
		PhysicalSortedMapStorageNode<CountKey,Count> node = physicalNodes.get(index);
		if(node==null){ return ListTool.createLinkedList(); }
		return ListTool.wrap(node);
	}
	
	@Override
	public List<PhysicalSortedMapStorageNode<CountKey,Count>> getPhysicalNodesForRange(Range<CountKey> range){
		if(range.getStart()==null && range.getEnd()==null){ 
			throw new IllegalArgumentException("must specify start or end value"); 
		}
		if(range.getStart()!=null && range.getEnd()!=null){
			if(ObjectTool.notEquals(range.getStart().getPeriodMs(), range.getEnd().getPeriodMs())){
				throw new IllegalArgumentException("cannot scan across multiple periods through this node");
			}
		}
		Integer index;
		if(range.getStart()!=null){ 
			index = indexByMs.get(range.getStart().getPeriodMs()); 
		}else{
			index = indexByMs.get(range.getEnd().getPeriodMs()); 
		}
		return ListTool.wrap(physicalNodes.get(index));
	}
	
	@Override
	public List<PhysicalSortedMapStorageNode<CountKey,Count>> getPhysicalNodesForPrefix(CountKey key, 
			boolean wildcardLastField){
		if(key.getPeriodMs()!=null){
			int nodeIndex = indexByMs.get(key.getPeriodMs());
			return ListTool.wrap(physicalNodes.get(nodeIndex)); 
		}
		return getPhysicalNodes();
	}
}

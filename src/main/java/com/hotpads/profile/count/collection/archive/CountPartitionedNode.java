package com.hotpads.profile.count.collection.archive;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.PartitionedSortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.Count.CountFielder;
import com.hotpads.profile.count.databean.key.CountKey;
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
		int n = Integer.valueOf(DrStringTool.retainDigits(t));
		String units = DrStringTool.retainLetters(t);
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
	public static final List<String> suffixes = DrListTool.create(
			"5s",  //    2764800 - that is 552960 records - ttl of 32 days
			"20s", //   11059200 - same 552960 records
			"1m",  //   33177600 - etc
			"5m",  //  165888000
			"20m", //  663552000 - something like 15 years
			"1h",  //
			"4h",  // 
			"1d"); // no ttl
	
	public static final List<String> flushPeriods = DrListTool.create("5s", "20s", "1m", "5m", "10m",  "30m", "1h", "1h");
	
	public static Map<String,Long> msBySuffix = new HashMap<>();
	public static Map<Long,String> suffixByMs = new HashMap<>();
	public static Map<Long,Long> flushPeriodByPeriod = new HashMap<>();
	static{
		for(int i=0; i < suffixes.size(); ++i){
			String suffix = suffixes.get(i);
			msBySuffix.put(suffix, getMs(suffix));
			suffixByMs.put(getMs(suffix), suffix);
			String flushPeriod = flushPeriods.get(i);
			flushPeriodByPeriod.put(getMs(suffix), getMs(flushPeriod));
		}
	}
	
	public static final Map<Long,Integer> indexByMs = new HashMap<>();
	static{
		int index = -1;
		for(String suffix : suffixes){ indexByMs.put(getMs(suffix), ++index); }
	}
	
	
	/********************************* constructor *************************************/
	
	public CountPartitionedNode(NodeFactory nodeFactory, Datarouter router, String clientName){
		super(Count.class, CountFielder.class, router);
		for(String suffix : suffixes){
			String tableName = TABLE_PREFIX + suffix;
			String entityName = ENTITY_PREFIX + suffix;
			Node<CountKey,Count> node = nodeFactory.create(clientName, tableName, entityName, Count.class,
					CountFielder.class, router, false);
			PhysicalSortedMapStorageNode<CountKey,Count> sortedNode = BaseDatarouter.cast(node);
			register(sortedNode);
		}
	}

	/********************************** static methods ******************************/
	
	public static long findNearestTablePeriodMs(long periodMs, int numPeriods){
		long result = d;
		for(int i = suffixes.size()-1; i>=0; i--){
			long totalMs = msBySuffix.get(suffixes.get(i));
			if(periodMs > numPeriods * totalMs){
				result = totalMs;
				break;
			}
			result = totalMs;
		}
		return result;
	}
	
	/********************************** required ************************************/
	
	@Override
	public PhysicalSortedMapStorageNode<CountKey,Count> getPhysicalNode(CountKey pk){
		return partitions.get(indexByMs.get(pk.getPeriodMs()));
	}
	
	@Override
	public List<PhysicalSortedMapStorageNode<CountKey,Count>> getPhysicalNodesForFirst(){
		PhysicalSortedMapStorageNode<CountKey,Count> firstNode = partitions.get(0);
		if(firstNode==null){ return null; }
		return DrListTool.wrap(firstNode);
	}
	
	@Override
	public List<PhysicalSortedMapStorageNode<CountKey,Count>> getPhysicalNodesForRange(Range<CountKey> range){
		if(range.getStart()==null && range.getEnd()==null){ 
			throw new IllegalArgumentException("must specify start or end value"); 
		}
		if(range.getStart()!=null && range.getEnd()!=null){
			if(DrObjectTool.notEquals(range.getStart().getPeriodMs(), range.getEnd().getPeriodMs())){
				throw new IllegalArgumentException("cannot scan across multiple periods through this node");
			}
		}
		Integer index;
		if(range.getStart()!=null){ 
			index = indexByMs.get(range.getStart().getPeriodMs()); 
		}else{
			index = indexByMs.get(range.getEnd().getPeriodMs()); 
		}
		return DrListTool.wrap(partitions.get(index));
	}
	
	@Override
	public SortedSetMultimap<PhysicalSortedMapStorageNode<CountKey,Count>,CountKey>
			getPrefixesByPhysicalNode(Collection<CountKey> prefixes, boolean wildcardLastField){
		SortedSetMultimap<PhysicalSortedMapStorageNode<CountKey,Count>,CountKey> prefixesByNode = TreeMultimap.create();
		for(CountKey prefix : DrIterableTool.nullSafe(prefixes)){
			int nodeIndex = indexByMs.get(prefix.getPeriodMs());
			prefixesByNode.put(partitions.get(nodeIndex), prefix);
		}
		return prefixesByNode;
	}
	
	@Override
	public boolean isSecondaryKeyPartitionAware(Key<CountKey> key){
		return knowsPartition(key);
	}
	
	@Override
	public List<PhysicalSortedMapStorageNode<CountKey,Count>> getPhysicalNodesForSecondaryKey(Key<CountKey> key) {
		if(!isSecondaryKeyPartitionAware(key)){ return getPhysicalNodes(); }
		CountKey countKey = (CountKey)key;
		Integer index = indexByMs.get(countKey.getPeriodMs());
		PhysicalSortedMapStorageNode<CountKey,Count> node = partitions.get(index);
		if(node==null){ return DrListTool.createLinkedList(); }
		return DrListTool.wrap(node);
	}
	
	/************************** helper **************************************/
	
	protected static boolean knowsPartition(Key<CountKey> key){
		//TODO don't assume it's the PK
		return ((CountKey)key).getPeriodMs()!=null;
	}
	
	public static class CountPartitionedNodeTest{
		@Test
		public void testCalculateTablePeriodMs(){
			long [] periods = {80000L, 7200000L, 86400000L, 604800000L, 3024000000L};
			long [] expectedPeriods = {5000L, 1200000L, 14400000L, 86400000L, 86400000L};
			for(int i=0; i < periods.length; i++){
				long tablePeriodMs = findNearestTablePeriodMs(periods[i], 5);
				Assert.assertEquals(expectedPeriods[i], tablePeriodMs);
				Assert.assertEquals(expectedPeriods[i], tablePeriodMs);
			}
		}
	}
}

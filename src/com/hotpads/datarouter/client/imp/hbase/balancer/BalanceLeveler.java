package com.hotpads.datarouter.client.imp.hbase.balancer;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;

/*
 * I: item
 * D: destination
 */
public class BalanceLeveler<I,D>{

	private SortedMap<I,D> destinationByItem;
	private long minAtDestination;
	private long maxAtDestination;
	private Map<D,Long> countByDestination;
	
	
	/************** construct ***************************/
	
	public BalanceLeveler(SortedMap<I,D> unleveledDestinationByItem){
		this.destinationByItem = new TreeMap<I,D>(unleveledDestinationByItem);
		updateCountByDestination();
	}
	
	
	/************* public methods ***********************/
	
	public SortedMap<I,D> getBalancedDestinationByItem(){
		while( ! isBalanced()){
			D mostLoadedDestination = getMostLoadedDestination();
			I firstItemAtDestination = getFirstItemAtDestinatioin(mostLoadedDestination);
			D leastLoadedDestination = getLeastLoadedDestination();
			//overwrite the region's D, thus moving it
			destinationByItem.put(firstItemAtDestination, leastLoadedDestination);
			updateCountByDestination();
		}
		return destinationByItem;
	}
	
	
	/*************** private methods **************************/
	
	private void updateCountByDestination(){
		for(Map.Entry<I,D> entry : destinationByItem.entrySet()){
			MapTool.increment(countByDestination, entry.getValue());
		}
		this.minAtDestination = ComparableTool.getFirst(countByDestination.values());
		this.maxAtDestination = ComparableTool.getLast(countByDestination.values());
	}
	
	private boolean isBalanced(){
		return maxAtDestination - minAtDestination <= 1;
	}
	
	private D getMostLoadedDestination(){
		for(Map.Entry<D,Long> entry : countByDestination.entrySet()){
			if(entry.getValue() == maxAtDestination){ return entry.getKey(); }
		}
		throw new IllegalArgumentException("max values out of sync");
	}
	
	private D getLeastLoadedDestination(){
		for(Map.Entry<D,Long> entry : countByDestination.entrySet()){
			if(entry.getValue() == minAtDestination){ return entry.getKey(); }
		}
		throw new IllegalArgumentException("min values out of sync");
	}
	
	
	private I getFirstItemAtDestinatioin(D destination){
		for(Map.Entry<I,D> entry : destinationByItem.entrySet()){
			if(ObjectTool.equals(destination, entry.getValue())){ return entry.getKey(); }
		}
		throw new IllegalArgumentException("item didn't exist in map");
	}
}

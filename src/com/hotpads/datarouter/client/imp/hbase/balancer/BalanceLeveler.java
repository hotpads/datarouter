package com.hotpads.datarouter.client.imp.hbase.balancer;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;

/*
 * I: item
 * D: destination
 */
public class BalanceLeveler<I,D>{
	private static Logger logger = Logger.getLogger(BalanceLeveler.class);

	private Collection<D> allDestinations;
	private SortedMap<I,D> destinationByItem;
	private long minAtDestination;
	private long maxAtDestination;
	private SortedMap<D,Long> countByDestination;
	
	
	/************** construct ***************************/
	
	public BalanceLeveler(Collection<D> allDestinations, SortedMap<I,D> unleveledDestinationByItem){
		this.allDestinations = CollectionTool.nullSafe(allDestinations);
		this.destinationByItem = new TreeMap<I,D>(unleveledDestinationByItem);
		this.countByDestination = MapTool.createTreeMap();//sorted easier for debugging
		updateCountByDestination();
	}
	
	
	/************* public methods ***********************/
	
	public SortedMap<I,D> getBalancedDestinationByItem(){
		while( ! isBalanced()){
			D mostLoadedDestination = getMostLoadedDestination();
			I itemToMove = takeFirstItemAtDestination(mostLoadedDestination);
			D leastLoadedDestination = getLeastLoadedDestination();
			//overwrite the region's D, thus moving it
			destinationByItem.put(itemToMove, leastLoadedDestination);
			updateCountByDestination();
		}
		return destinationByItem;
	}
	
	
	/*************** private methods **************************/
	
	private void updateCountByDestination(){
		countByDestination.clear();
		for(Map.Entry<I,D> entry : destinationByItem.entrySet()){
			MapTool.increment(countByDestination, entry.getValue());
		}
		ensureAllDestinationsInCountByDestination();
		this.minAtDestination = ComparableTool.getFirst(countByDestination.values());
		this.maxAtDestination = ComparableTool.getLast(countByDestination.values());
		logger.warn(countByDestination);
		logger.warn("minAtDestination"+minAtDestination);
		logger.warn("maxAtDestination"+maxAtDestination);
	}
	
	private void ensureAllDestinationsInCountByDestination(){
		for(D d : allDestinations){
			if(!countByDestination.containsKey(d)){
				countByDestination.put(d, 0L);
			}
		}
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
	
	
	private I takeFirstItemAtDestination(D destination){
		for(Map.Entry<I,D> entry : destinationByItem.entrySet()){
			if(ObjectTool.equals(destination, entry.getValue())){ 
				destinationByItem.remove(entry.getKey());
				return entry.getKey(); 
			}
		}
		throw new IllegalArgumentException("item didn't exist in map:"+destination);
	}
}

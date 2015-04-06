package com.hotpads.datarouter.client.imp.hbase.balancer;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrMapTool;

/*
 * I: item
 * D: destination
 */
public class BalanceLeveler<I,D>{
//	private static Logger logger = LoggerFactory.getLogger(BalanceLeveler.class);

	private Collection<D> allDestinations;
	private SortedMap<I,D> destinationByItem;
	private long minAtDestination;
	private long maxAtDestination;
	private SortedMap<D,Long> countByDestination;
	
	
	/************** construct ***************************/
	
	public BalanceLeveler(Collection<D> allDestinations, SortedMap<I,D> unleveledDestinationByItem){
		this.allDestinations = DrCollectionTool.nullSafe(allDestinations);
		this.destinationByItem = new TreeMap<I,D>(unleveledDestinationByItem);
		this.countByDestination = new TreeMap<>();//sorted easier for debugging
		updateCountByDestination();
	}
	
	
	/************* public methods ***********************/
	
	public SortedMap<I,D> getBalancedDestinationByItem(){
		while( ! isBalanced()){
			D mostLoadedDestination = getMostLoadedDestination();
			I itemToMove = DrMapTool.getFirstKeyWhereValueEquals(destinationByItem, mostLoadedDestination);
			D leastLoadedDestination = getLeastLoadedDestination();
			//overwrite the item's destination, thus moving it
			destinationByItem.put(itemToMove, leastLoadedDestination);
			updateCountByDestination();
		}
		return destinationByItem;
	}
	
	
	/*************** private methods **************************/
	
	private void updateCountByDestination(){
		countByDestination.clear();
		for(Map.Entry<I,D> entry : destinationByItem.entrySet()){
			DrMapTool.increment(countByDestination, entry.getValue());
		}
		ensureAllDestinationsInCountByDestination();
		this.minAtDestination = DrComparableTool.getFirst(countByDestination.values());
		this.maxAtDestination = DrComparableTool.getLast(countByDestination.values());
//		logger.warn(countByDestination);
//		logger.warn("minAtDestination"+minAtDestination);
//		logger.warn("maxAtDestination"+maxAtDestination);
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
	
}

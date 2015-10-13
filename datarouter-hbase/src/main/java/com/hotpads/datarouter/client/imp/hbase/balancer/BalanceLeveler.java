package com.hotpads.datarouter.client.imp.hbase.balancer;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrHashMethods;
import com.hotpads.datarouter.util.core.DrMapTool;

/*
 * I: item
 * D: destination
 */
public class BalanceLeveler<I,D extends ServerName>{
//	private static Logger logger = LoggerFactory.getLogger(BalanceLeveler.class);

	private final Collection<D> allDestinations;
	private final SortedMap<I,D> destinationByItem;
	
	private long minAtDestination;
	private long maxAtDestination;
	private SortedMap<D,Long> countByDestination;//this must be sorted to keep the serverNames in teh same order
	
	
	/************** construct ***************************/
	
	public BalanceLeveler(Collection<D> allDestinations, SortedMap<I,D> unleveledDestinationByItem,
			String randomSeed){
		this.allDestinations = DrCollectionTool.nullSafe(allDestinations);
		this.destinationByItem = new TreeMap<I,D>(unleveledDestinationByItem);
		
		Comparator<D> randomServerNameComparator = new TablePseudoRandomComparator<>(randomSeed);
		this.countByDestination = new TreeMap<>(randomServerNameComparator);
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
	
	
	/*************** pseudo-random comparator *********************************/
	
	/*
	 * for a given table, scramble the order of ServerNames in the CountByDestinion map. this will prevent all tables
	 * from sending their extra tables to the same servers 
	 */
	private static class TablePseudoRandomComparator<D extends ServerName> implements Comparator<D>{
		private final String randomSeed;
		
		public TablePseudoRandomComparator(String randomSeed){
			this.randomSeed = randomSeed;
		}

		@Override
		public int compare(ServerName serverA, ServerName serverB){
			long serverASort = DrHashMethods.longMD5DJBHash(randomSeed + serverA.getHostAndPort());
			long serverBSort = DrHashMethods.longMD5DJBHash(randomSeed + serverB.getHostAndPort());
			return (int)(serverASort - serverBSort);
		}
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
			if(entry.getValue() == maxAtDestination) {
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("max values out of sync");
	}
	
	
	private D getLeastLoadedDestination(){
		for(Map.Entry<D,Long> entry : countByDestination.entrySet()){
			if(entry.getValue() == minAtDestination) {
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("min values out of sync");
	}
	
}

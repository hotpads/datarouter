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
public class HBaseBalanceLeveler<I>{
//	private static Logger logger = LoggerFactory.getLogger(BalanceLeveler.class);

	private final Collection<ServerName> allDestinations;
	private final SortedMap<I,ServerName> destinationByItem;
	
	private long minAtDestination;
	private long maxAtDestination;
	private SortedMap<ServerName,Long> countByDestination;//must be sorted to keep the serverNames in the same order
	
	
	/************** construct ***************************/
	
	public HBaseBalanceLeveler(Collection<ServerName> allDestinations,
			SortedMap<I,ServerName> unleveledDestinationByItem, String randomSeed){
		this.allDestinations = DrCollectionTool.nullSafe(allDestinations);
		this.destinationByItem = new TreeMap<>(unleveledDestinationByItem);
		
		Comparator<ServerName> randomServerNameComparator = new TablePseudoRandomComparator(randomSeed);
		this.countByDestination = new TreeMap<>(randomServerNameComparator);
		updateCountByDestination();
	}
	
	
	/************* public methods ***********************/
	
	public SortedMap<I,ServerName> getBalancedDestinationByItem(){
		while( ! isBalanced()){
			ServerName mostLoadedDestination = getMostLoadedDestination();
			I itemToMove = DrMapTool.getFirstKeyWhereValueEquals(destinationByItem, mostLoadedDestination);
			ServerName leastLoadedDestination = getLeastLoadedDestination();
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
	private static class TablePseudoRandomComparator implements Comparator<ServerName>{
		private final String randomSeed;
		
		public TablePseudoRandomComparator(String randomSeed){
			this.randomSeed = randomSeed;
		}

		@Override
		public int compare(ServerName serverA, ServerName serverB){
			long serverASort = DrHashMethods.longDJBHash(randomSeed + serverA.getHostAndPort());
			long serverBSort = DrHashMethods.longDJBHash(randomSeed + serverB.getHostAndPort());
			return (int)(serverASort - serverBSort);
		}
	}
	
	
	/*************** private methods **************************/
	
	private void updateCountByDestination(){
		countByDestination.clear();
		for(Map.Entry<I,ServerName> entry : destinationByItem.entrySet()){
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
		for(ServerName d : allDestinations){
			if(!countByDestination.containsKey(d)){
				countByDestination.put(d, 0L);
			}
		}
	}
	
	
	private boolean isBalanced(){
		return maxAtDestination - minAtDestination <= 1;
	}
	
	
	private ServerName getMostLoadedDestination(){
		for(Map.Entry<ServerName,Long> entry : countByDestination.entrySet()){
			if(entry.getValue() == maxAtDestination) {
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("max values out of sync");
	}
	
	
	private ServerName getLeastLoadedDestination(){
		for(Map.Entry<ServerName,Long> entry : countByDestination.entrySet()){
			if(entry.getValue() == minAtDestination) {
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("min values out of sync");
	}
	
}

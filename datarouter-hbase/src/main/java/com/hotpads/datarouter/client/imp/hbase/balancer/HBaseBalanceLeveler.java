package com.hotpads.datarouter.client.imp.hbase.balancer;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.hbase.ServerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrHashMethods;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.datarouter.util.core.DrObjectTool;

/*
 * I: item
 * D: destination
 */
public class HBaseBalanceLeveler<I>{
	private static final Logger logger = LoggerFactory.getLogger(HBaseBalanceLeveler.class);
	
	public static final boolean PSEUDO_RANDOM_LEVELING = false;
	
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
		
		this.countByDestination = PSEUDO_RANDOM_LEVELING ? new TreeMap<>(new TablePseudoRandomHostAndPortComparator(
				randomSeed)) : new TreeMap<>(new HostAndPortComparator());
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
	
	
	/*************** ServerName comparator *********************************/

	private static class HostAndPortComparator implements Comparator<ServerName>{
		@Override
		public int compare(ServerName serverA, ServerName serverB){
			return serverA.getHostAndPort().compareTo(serverB.getHostAndPort());
		}
	}
	
	
	/*
	 * for a given table, scramble the order of ServerNames in the CountByDestinion map. this will prevent all tables
	 * from sending their extra tables to the same servers 
	 */
	//WARNING: something is wrong with this and causes duplicate entries in a TreeMap
	private static class TablePseudoRandomHostAndPortComparator implements Comparator<ServerName>{
		private final String randomSeed;
		
		public TablePseudoRandomHostAndPortComparator(String randomSeed){
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
		destinationByItem.values().forEach(destination -> DrMapTool.increment(countByDestination, destination));
		if(countByDestination.size() > allDestinations.size()){
			throw new IllegalStateException("countByDestination.size() is " + countByDestination.size()
					+ " which is greater than " + allDestinations.size());
		}
		ensureAllDestinationsInCountByDestination();
		if(DrObjectTool.notEquals(countByDestination.size(), allDestinations.size())) {
			throw new IllegalStateException("countByDestination.size() is " + countByDestination.size()
					+ " but should be " + allDestinations.size());
		}
		this.minAtDestination = DrComparableTool.getFirst(countByDestination.values());
		this.maxAtDestination = DrComparableTool.getLast(countByDestination.values());
	}
	
	
	private void ensureAllDestinationsInCountByDestination(){
		allDestinations.stream().forEach(destination -> countByDestination.putIfAbsent(destination, 0L));
	}
	
	
	private boolean isBalanced(){
		return maxAtDestination - minAtDestination <= 1;
	}
	
	
	private ServerName getMostLoadedDestination(){
		for(Map.Entry<ServerName,Long> entry : countByDestination.entrySet()){
			if(DrObjectTool.equals(entry.getValue(), maxAtDestination)){
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("max values out of sync");
	}
	
	
	private ServerName getLeastLoadedDestination(){
		for(Map.Entry<ServerName,Long> entry : countByDestination.entrySet()){
			if(DrObjectTool.equals(entry.getValue(), minAtDestination)){
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("min values out of sync");
	}
	
	
	/********************* tests ************************/
	
	public static class HBaseBalanceLevelerTests{
		@Test
		public void testComparator(){
			final int uniqueServers = 7;
			Set<ServerName> serverNames = new TreeSet<>(new TablePseudoRandomHostAndPortComparator("MyTableName"));
			for(int i=0; i < 100; ++i){
				serverNames.add(new ServerName("SomeServer" + i % uniqueServers, 123, i));
			}
			Assert.assertEquals(serverNames.size(), uniqueServers);
		}
	}
}

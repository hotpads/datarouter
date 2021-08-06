/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.hbase.balancer;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.HashMethods;
import io.datarouter.util.lang.ObjectTool;

public class HBaseBalanceLeveler<I>{

	public static final boolean PSEUDO_RANDOM_LEVELING = false;

	private final Collection<ServerName> allDestinations;
	private final SortedMap<I,ServerName> destinationByItem;

	private long minAtDestination;
	private long maxAtDestination;
	private SortedMap<ServerName,Long> countByDestination;//must be sorted to keep the serverNames in the same order

	public HBaseBalanceLeveler(
			Collection<ServerName> allDestinations,
			SortedMap<I,ServerName> unleveledDestinationByItem,
			String randomSeed){
		this.allDestinations = allDestinations;
		this.destinationByItem = new TreeMap<>(unleveledDestinationByItem);

		this.countByDestination = PSEUDO_RANDOM_LEVELING
				? new TreeMap<>(new TablePseudoRandomHostAndPortComparator(randomSeed))
				: new TreeMap<>(new HostAndPortComparator());
		updateCountByDestination();
	}

	public SortedMap<I,ServerName> getBalancedDestinationByItem(){
		while(!isBalanced()){
			ServerName mostLoadedDestination = getMostLoadedDestination();
			I itemToMove = Scanner.of(destinationByItem.entrySet())
					.include(entry -> Objects.equals(entry.getValue(), mostLoadedDestination))
					.findFirst()
					.map(Entry::getKey)
					.orElse(null);
			ServerName leastLoadedDestination = getLeastLoadedDestination();
			//overwrite the item's destination, thus moving it
			destinationByItem.put(itemToMove, leastLoadedDestination);
			updateCountByDestination();
		}
		return destinationByItem;
	}

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
	public static class TablePseudoRandomHostAndPortComparator implements Comparator<ServerName>{

		private final String randomSeed;

		public TablePseudoRandomHostAndPortComparator(String randomSeed){
			this.randomSeed = randomSeed;
		}

		@Override
		public int compare(ServerName serverA, ServerName serverB){
			long serverASort = HashMethods.longDjbHash(randomSeed + serverA.getHostAndPort());
			long serverBSort = HashMethods.longDjbHash(randomSeed + serverB.getHostAndPort());
			return (int)(serverASort - serverBSort);
		}

	}

	private void updateCountByDestination(){
		countByDestination.clear();
		destinationByItem.values().forEach(destination -> countByDestination.merge(destination, 1L, Long::sum));
		if(countByDestination.size() > allDestinations.size()){
			throw new IllegalStateException("countByDestination.size() is " + countByDestination.size()
					+ " which is greater than " + allDestinations.size());
		}
		ensureAllDestinationsInCountByDestination();
		if(ObjectTool.notEquals(countByDestination.size(), allDestinations.size())){
			throw new IllegalStateException("countByDestination.size() is " + countByDestination.size()
					+ " but should be " + allDestinations.size());
		}
		this.minAtDestination = countByDestination.values().stream()
				.mapToLong(Long::longValue)
				.min()
				.orElse(0);
		this.maxAtDestination = countByDestination.values().stream()
				.mapToLong(Long::longValue)
				.max()
				.orElse(0);
	}

	private void ensureAllDestinationsInCountByDestination(){
		allDestinations.forEach(destination -> countByDestination.putIfAbsent(destination, 0L));
	}

	private boolean isBalanced(){
		return maxAtDestination - minAtDestination <= 1;
	}

	private ServerName getMostLoadedDestination(){
		for(Entry<ServerName,Long> entry : countByDestination.entrySet()){
			if(Objects.equals(entry.getValue(), maxAtDestination)){
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("max values out of sync");
	}

	private ServerName getLeastLoadedDestination(){
		for(Entry<ServerName,Long> entry : countByDestination.entrySet()){
			if(Objects.equals(entry.getValue(), minAtDestination)){
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("min values out of sync");
	}

}

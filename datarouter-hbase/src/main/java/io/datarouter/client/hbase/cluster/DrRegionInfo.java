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
package io.datarouter.client.hbase.cluster;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.RegionLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.compaction.DrhCompactionScheduler;
import io.datarouter.client.hbase.compaction.HBaseCompactionInfo;
import io.datarouter.client.hbase.node.nonentity.HBaseReaderNode;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.model.field.FieldSet;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.lang.ClassTool;
import io.datarouter.util.number.NumberFormatter;

public class DrRegionInfo<PK extends PrimaryKey<PK>> implements Comparable<DrRegionInfo<?>>{
	private static final Logger logger = LoggerFactory.getLogger(DrRegionInfo.class);

	private final Integer regionNum;
	private final String tableName;
	private final String name;
	private final HRegionInfo regionInfo;
	private final ServerName serverName;
	private final Function<byte[],FieldSet<?>> keyParser;
	private final Integer partition;
	private final RegionLoad load;
	private final DrhCompactionScheduler<PK> compactionScheduler;
	private final HBaseCompactionInfo compactionInfo;
	private final EntityFieldInfo<?,?> entityFieldInfo;

	private ServerName balancerDestinationServer;

	public DrRegionInfo(
			Integer regionNum,
			String tableName,
			HRegionInfo regionInfo,
			ServerName serverName,
			PhysicalNode<?,?,?> nodeOrAdapter,
			RegionLoad load,
			HBaseCompactionInfo compactionInfo,
			EntityFieldInfo<?,?> entityFieldInfo){
		this.regionNum = regionNum;
		this.tableName = tableName;
		this.compactionInfo = compactionInfo;
		this.name = new String(regionInfo.getRegionName());
		this.regionInfo = regionInfo;
		this.serverName = serverName;
		// unwrap from adapters to expose real implementation and the getResultParser method
		// it's ok to do so because the node is used only for non rpc byte parsing methods
		Node<?,?,?> node = NodeTool.getUnderlyingNode(nodeOrAdapter);
		HBaseReaderNode<?,?,?,?,?> nonEntityNode = (HBaseReaderNode<?,?,?,?,?>)node;
		keyParser = nonEntityNode.getResultParser()::toPk;
		this.load = load;
		this.compactionScheduler = new DrhCompactionScheduler<>(compactionInfo, this);
		this.entityFieldInfo = entityFieldInfo;
		this.partition = calculatePartition(regionInfo.getStartKey());//after setting entityFieldInfo
	}

	private Conditional<Optional<FieldSet<?>>> getKey(byte[] bytes){
		if(bytes.length == 0){
			return Conditional.success(Optional.empty());
		}
		try{
			return Conditional.success(Optional.of(keyParser.apply(bytes)));
		}catch(Exception e){
			return Conditional.failure(e);
		}
	}

	private Integer calculatePartition(byte[] bytes){
		if(ArrayTool.isEmpty(bytes)){
			return 0;
		}
		return entityFieldInfo.getEntityPartitioner().parsePartitionFromBytes(bytes);
	}

	public ServerName getConsistentHashServerName(){
		return balancerDestinationServer;
	}

	public boolean isNotOnAnyServer(){
		return serverName == null;
	}

	public boolean isOnCorrectServer(){
		try{
			return Objects.equals(serverName, balancerDestinationServer);
		}catch(NullPointerException npe){
			logger.warn("", npe);
		}
		return true;//default: leave it where it is
	}

	public ServerName getHBaseServerName(){
		return serverName;
	}

	public ServerName getBalancerDestinationHBaseServerName(){
		return balancerDestinationServer;
	}

	public String getServerName(){
		return serverName.getServerName();
	}

	//used in hbaseTableRegions.jsp
	public String getDisplayServerName(){
		//doesn't account for multiple servers per node
		return compactionInfo.getDisplayServerName(serverName.getHostname());//hServerInfo.getHostname();
	}

	//used in hbaseTableRegions.jsp
	public String getConsistentHashDisplayServerName(){
		//doesn't account for multiple servers per node
		return compactionInfo.getDisplayServerName(balancerDestinationServer.getHostname());//hServerInfo.getHostname();
	}

	//used in hbaseTableRegions.jsp
	public String getNumKeyValuesWithCompactionPercent(){
		if(load == null){
			return "?";
		}
		long totalKvs = load.getTotalCompactingKVs();
		String totalKvsString = NumberFormatter.addCommas(totalKvs);
		long compactingKvs = load.getCurrentCompactedKVs();
		if(totalKvs == compactingKvs){
			return totalKvsString;
		}
		int percentCompacted = (int)((double)100 * (double)compactingKvs / totalKvs);
		return totalKvsString + " [" + percentCompacted + "%]";
	}

	//used in hbaseTableRegions.jsp
	public Integer getRegionNum(){
		return regionNum;
	}

	public String getTableName(){
		return tableName;
	}

	public String getName(){
		return name;
	}

	//used in hbaseTableRegions.jsp
	public String getStartRowKey(){
		return Bytes.toStringBinary(regionInfo.getStartKey());
	}

	//used in hbaseTableRegions.jsp
	public Object getStartKeyString(){
		return getStartKeyTyped()
				.<Object>map(opt -> opt.orElse(null))
				.ifFailure(e -> logger.warn("", e))
				.orElseGet(e -> e);
	}

	public Conditional<Optional<FieldSet<?>>> getStartKeyTyped(){
		byte[] bytes = regionInfo.getStartKey();
		return getKey(bytes);
	}

	//used in hbaseTableRegions.jsp
	public String getEndRowKey(){
		return Bytes.toStringBinary(regionInfo.getEndKey());
	}

	//used in hbaseTableRegions.jsp
	public Object getEndKeyString(){
		return getEndKeyTyped()
				.<Object>map(opt -> opt.orElse(null))
				.ifFailure(e -> logger.warn("", e))
				.orElseGet(e -> e);
	}

	public Conditional<Optional<FieldSet<?>>> getEndKeyTyped(){
		byte[] bytes = regionInfo.getEndKey();
		return getKey(bytes);
	}

	public Integer getPartition(){
		return partition;
	}

	public HRegionInfo getRegion(){
		return regionInfo;
	}

	public RegionLoad getLoad(){
		return load;
	}

	//used in hbaseTableRegions.jsp
	public DrhCompactionScheduler<PK> getCompactionScheduler(){
		return compactionScheduler;
	}

	public void setBalancerDestinationServer(ServerName balancerDestinationServer){
		this.balancerDestinationServer = Objects.requireNonNull(balancerDestinationServer);
	}

	/*------------- Object, Comparable ----------------*/

	@Override
	public String toString(){
		return regionInfo.getEncodedName();
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(ClassTool.differentClass(this, obj)){
			return false;
		}
		DrRegionInfo<?> that = (DrRegionInfo<?>)obj;
		return Objects.equals(regionInfo.getEncodedName(), that.regionInfo.getEncodedName());
	}

	@Override
	public int hashCode(){
		return regionInfo.getEncodedName().hashCode();
	}

	@Override
	public int compareTo(DrRegionInfo<?> other){
		return Bytes.compareTo(regionInfo.getStartKey(), other.getRegion().getStartKey());
	}

}

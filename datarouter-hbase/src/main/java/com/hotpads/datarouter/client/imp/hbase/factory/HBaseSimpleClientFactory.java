package com.hotpads.datarouter.client.imp.hbase.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.HBaseStaticContext;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.pool.HTableExecutorServicePool;
import com.hotpads.datarouter.client.imp.hbase.pool.HTablePool;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Twin;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseSimpleClientFactory
implements ClientFactory{
	private static final Logger logger = LoggerFactory.getLogger(HBaseSimpleClientFactory.class);

	//default table configuration settings for new tables
	private static final long
			DEFAULT_MAX_FILE_SIZE_BYTES = 1L * 4 * 1024 * 1024 * 1024,//cast to long before overflowing int
			DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES = 1L * 256 * 1024 * 1024;//cast to long before overflowing int

	//these are used for databeans with no values outside the PK.  we fake a value as we need at least 1 cell in a row
	public static final byte[] DEFAULT_FAMILY_QUALIFIER = new byte[]{(byte)'a'};
	public static final String DUMMY_COL_NAME = new String(new byte[]{0});


	/********************* fields *******************************/

	private final Datarouter datarouter;
	private final String clientName;
	private final Set<String> configFilePaths;
	private final List<Properties> multiProperties;
	private final HBaseOptions options;
	private final ClientAvailabilitySettings clientAvailabilitySettings;

	//we cannot finalize these as they are created in a background thread for faster application boot time
	private Configuration hbaseConfig;
	private HBaseAdmin hbaseAdmin;

	public HBaseSimpleClientFactory(Datarouter datarouter, String clientName, ClientAvailabilitySettings
			clientAvailabilitySettings){
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.datarouter = datarouter;
		this.clientName = clientName;
		this.configFilePaths = datarouter.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.options = new HBaseOptions(multiProperties, clientName);
	}


	@Override
	public HBaseClient call(){
		HBaseClientImp newClient = null;
		try{
			logger.info("activating HBase client "+clientName);
			PhaseTimer timer = new PhaseTimer(clientName);

			String zkQuorum = options.zookeeperQuorum();
			hbaseConfig = HBaseStaticContext.CONFIG_BY_ZK_QUORUM.get(zkQuorum);
			if(hbaseConfig==null){
				hbaseConfig = HBaseConfiguration.create();
				hbaseConfig.set(HConstants.ZOOKEEPER_QUORUM, zkQuorum);
			}
			hbaseAdmin = new HBaseAdmin(hbaseConfig);
			if(hbaseAdmin.getConnection().isClosed()){
				HBaseStaticContext.CONFIG_BY_ZK_QUORUM.remove(zkQuorum);
				HBaseStaticContext.ADMIN_BY_CONFIG.remove(hbaseConfig);
				hbaseConfig = null;
				hbaseAdmin = null;
				String log = "couldn't open connection because hBaseAdmin.getConnection().isClosed()";
				logger.warn(log);
				throw new UnavailableException(log);
			}
			HBaseStaticContext.CONFIG_BY_ZK_QUORUM.put(zkQuorum, hbaseConfig);
			HBaseStaticContext.ADMIN_BY_CONFIG.put(hbaseConfig, hbaseAdmin);

			//databean config
			Pair<HTablePool,Map<String,Class<? extends PrimaryKey<?>>>> htablePoolAndPrimaryKeyByTableName
					= initTables();
			timer.add("init HTables");

			newClient = new HBaseClientImp(clientName, hbaseConfig, hbaseAdmin, htablePoolAndPrimaryKeyByTableName
					.getLeft(), htablePoolAndPrimaryKeyByTableName.getRight(), clientAvailabilitySettings);
			logger.warn(timer.add("done").toString());
		}catch(ZooKeeperConnectionException e){
			throw new UnavailableException(e);
		}catch(MasterNotRunningException e){
			throw new UnavailableException(e);
		}catch(IOException e){
			throw new UnavailableException(e);
		}
		return newClient;
	}


	/********************** private ***************************/

	private Pair<HTablePool,Map<String,Class<? extends PrimaryKey<?>>>> initTables(){
		List<String> tableNames = new ArrayList<>();
		Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName = new HashMap<>();
		Map<String,PhysicalNode<?,?>> nodeByTableName = new TreeMap<>();
		Collection<PhysicalNode<?,?>> physicalNodes = datarouter.getNodes().getPhysicalNodesForClient(clientName);
		for(PhysicalNode<?,?> node : physicalNodes){
			tableNames.add(node.getTableName());
			primaryKeyClassByName.put(node.getTableName(), node.getPrimaryKeyType());
			nodeByTableName.put(node.getTableName(), node);
		}

		try{
			boolean checkTables = options.checkTables();
			boolean createTables = options.createTables();
			if(checkTables || createTables){
				for(String tableName : DrIterableTool.nullSafe(tableNames)){
					byte[] tableNameBytes = StringByteTool.getUtf8Bytes(tableName);
					if(createTables && !hbaseAdmin.tableExists(tableName)){
						logger.warn("table " + tableName + " not found, creating it");
						HTableDescriptor htable = new HTableDescriptor(tableName);
						htable.setMaxFileSize(DEFAULT_MAX_FILE_SIZE_BYTES);
						htable.setMemStoreFlushSize(DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES);
						HColumnDescriptor family = new HColumnDescriptor(DEFAULT_FAMILY_QUALIFIER);
						family.setMaxVersions(1);
						family.setBloomFilterType(BloomType.NONE);
						family.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
						family.setCompressionType(Algorithm.GZ);
						htable.addFamily(family);
						byte[][] splitPoints = getSplitPoints(nodeByTableName.get(tableName));
						if(DrArrayTool.isEmpty(splitPoints)
								|| DrArrayTool.isEmpty(splitPoints[0])){//a single empty byte array
							hbaseAdmin.createTable(htable);
						}else{
							//careful, as throwing strange split points in here can crash master
							// and corrupt meta table
							hbaseAdmin.createTable(htable, splitPoints);
						}
						logger.warn("created table " + tableName);
					}else if(checkTables){
						if(!hbaseAdmin.tableExists(tableNameBytes)){
							logger.warn("table " + tableName + " not found");
							break;
						}
					}
				}
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}

		HTablePool pool = new HTableExecutorServicePool(options, hbaseAdmin, clientName, primaryKeyClassByName);
		return Pair.create(pool, primaryKeyClassByName);
	}


	// this currently gets ignored on an empty table since there are no regions to split
	private byte[][] getSplitPoints(PhysicalNode<?,?> node){
		if(node.getPhysicalNodeIfApplicable() instanceof HBaseSubEntityReaderNode){
			HBaseSubEntityReaderNode<?,?,?,?,?> subEntityNode = (HBaseSubEntityReaderNode<?,?,?,?,?>)node;
			EntityFieldInfo<?,?> entityFieldInfo = subEntityNode.getEntityFieldInfo();
			EntityPartitioner<?> partitioner = entityFieldInfo.getEntityPartitioner();
			//remember to skip the first partition
			int numSplitPoints = partitioner.getNumPartitions() - 1;
			byte[][] splitPoints = new byte[numSplitPoints][];
			for(int i=1; i < partitioner.getAllPrefixes().size(); ++i){
				splitPoints[i-1] = partitioner.getPrefix(i);
			}
			return splitPoints;
		}else if(node.getPhysicalNodeIfApplicable() instanceof HBaseReaderNode){
			DatabeanFieldInfo<?,?,?> fieldInfo = node.getFieldInfo();
			ScatteringPrefix sampleScatteringPrefix = fieldInfo.getSampleScatteringPrefix();
			if(sampleScatteringPrefix==null){
				return null;
			}
			List<List<Field<?>>> allPrefixes = sampleScatteringPrefix.getAllPossibleScatteringPrefixes();
			List<byte[]> splitPoints = new ArrayList<>();
			for(List<Field<?>> prefixFields : allPrefixes){
				Twin<ByteRange> range = HBaseQueryBuilder.getStartEndBytesForPrefix(prefixFields, false);
				if( ! isSingleEmptyByte(range.getLeft().toArray())){
					splitPoints.add(range.getLeft().toArray());
				}
			}
			return splitPoints.toArray(new byte[splitPoints.size()][]);
		}else{
			throw new IllegalArgumentException("Node should be one of the above two types");
		}
	}


	private boolean isSingleEmptyByte(byte[] bytes){
		if(DrArrayTool.length(bytes)!=1){
			return false;
		}
		return bytes[0] == Byte.MIN_VALUE;
	}

}

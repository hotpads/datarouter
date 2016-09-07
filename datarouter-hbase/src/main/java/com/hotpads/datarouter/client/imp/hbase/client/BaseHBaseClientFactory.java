package com.hotpads.datarouter.client.imp.hbase.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.hbase.BaseHBaseClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.pool.HBaseTableExecutorServicePool;
import com.hotpads.datarouter.client.imp.hbase.pool.HBaseTablePool;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
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

public abstract class BaseHBaseClientFactory
implements ClientFactory{
	private static final Logger logger = LoggerFactory.getLogger(BaseHBaseClientFactory.class);

	//default table configuration settings for new tables
	private static final long
			DEFAULT_MAX_FILE_SIZE_BYTES = 1L * 4 * 1024 * 1024 * 1024,//cast to long before overflowing int
			DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES = 1L * 256 * 1024 * 1024;//cast to long before overflowing int

	//these are used for databeans with no values outside the PK.  we fake a value as we need at least 1 cell in a row
	public static final byte[] DEFAULT_FAMILY_QUALIFIER = new byte[]{(byte)'a'};
	public static final String DUMMY_COL_NAME = new String(new byte[]{0});


	/********************* fields *******************************/

	private final Datarouter datarouter;
	protected final String clientName;
	private final Set<String> configFilePaths;
	protected final List<Properties> multiProperties;
	protected final HBaseOptions hbaseOptions;
	protected final ClientAvailabilitySettings clientAvailabilitySettings;
	protected final ExecutorService executor;
	private final BaseHBaseClientType clientType;

	public BaseHBaseClientFactory(Datarouter datarouter, String clientName,
			ClientAvailabilitySettings clientAvailabilitySettings, ExecutorService executor,
			BaseHBaseClientType clientType){
		this.datarouter = datarouter;
		this.clientName = clientName;
		this.clientType = clientType;
		this.configFilePaths = datarouter.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.hbaseOptions = new HBaseOptions(multiProperties, clientName);
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.executor = executor;
	}

	protected abstract Connection makeConnection();

	@Override
	public HBaseClient call(){
		logger.info("activating HBase client " + clientName);
		PhaseTimer timer = new PhaseTimer(clientName);
		Connection connection = makeConnection();
		Admin admin;
		try{
			admin = connection.getAdmin();
		}catch(IOException e){
			throw new RuntimeException(e);
		}

		// databean config
		Pair<HBaseTablePool,Map<String,Class<? extends PrimaryKey<?>>>> htablePoolAndPrimaryKeyByTableName = initTables(
				connection, admin);
		timer.add("init HTables");

		logger.warn(timer.add("done").toString());
		return new HBaseClientImp(clientName, connection, admin, htablePoolAndPrimaryKeyByTableName.getLeft(),
				htablePoolAndPrimaryKeyByTableName.getRight(), clientAvailabilitySettings, executor, clientType);
	}


	private Pair<HBaseTablePool,Map<String,Class<? extends PrimaryKey<?>>>> initTables(Connection connection,
			Admin admin){
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
			boolean checkTables = hbaseOptions.checkTables();
			boolean createTables = hbaseOptions.createTables();
			if(checkTables || createTables){
				for(String tableName : DrIterableTool.nullSafe(tableNames)){
					byte[] tableNameBytes = StringByteTool.getUtf8Bytes(tableName);
					if(createTables && !admin.tableExists(TableName.valueOf(tableName))){
						logger.warn("table " + tableName + " not found, creating it");
						HTableDescriptor htable = new HTableDescriptor(TableName.valueOf(tableName));
						DatabeanFieldInfo<?,?,?> fieldInfo = nodeByTableName.get(tableName).getFieldInfo();
						htable.setMaxFileSize(DEFAULT_MAX_FILE_SIZE_BYTES);
						htable.setMemStoreFlushSize(DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES);
						HColumnDescriptor family = new HColumnDescriptor(DEFAULT_FAMILY_QUALIFIER);
						family.setMaxVersions(1);
						family.setBloomFilterType(BloomType.NONE);
						family.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
						family.setCompressionType(Algorithm.GZ);
						long ttlMs = fieldInfo.getTtlMs().orElse((long)HConstants.FOREVER);
						family.setTimeToLive((int)ttlMs);
						htable.addFamily(family);
						byte[][] splitPoints = getSplitPoints(nodeByTableName.get(tableName));
						if(DrArrayTool.isEmpty(splitPoints)
								|| DrArrayTool.isEmpty(splitPoints[0])){//a single empty byte array
							admin.createTable(htable);
						}else{
							//careful, as throwing strange split points in here can crash master
							// and corrupt meta table
							admin.createTable(htable, splitPoints);
						}
						logger.warn("created table " + tableName);
					}else if(checkTables){
						if(!admin.tableExists(TableName.valueOf(tableNameBytes))){
							logger.warn("table " + tableName + " not found");
							break;
						}
					}
				}
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}

		HBaseTablePool pool = new HBaseTableExecutorServicePool(hbaseOptions, connection, clientName,
				primaryKeyClassByName, clientType);
		return new Pair<>(pool, primaryKeyClassByName);
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

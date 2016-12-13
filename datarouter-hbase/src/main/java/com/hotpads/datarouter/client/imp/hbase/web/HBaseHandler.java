package com.hotpads.datarouter.client.imp.hbase.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.browse.RoutersHandler;
import com.hotpads.datarouter.browse.dto.RouterParams;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.balancer.HBaseBalancerFactory;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrServerInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrServerList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrTableSettings;
import com.hotpads.datarouter.client.imp.hbase.compaction.HBaseCompactionInfo;
import com.hotpads.datarouter.client.imp.hbase.util.ServerNameTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.ThreadTool;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.http.RequestTool;

public class HBaseHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(HBaseHandler.class);

	private static final String PATH_JSP_HBASE = "/jsp/admin/datarouter/hbase/";

	public static final String
		PARAM_tableName = "tableName",
		PARAM_destinationTableName = "destinationTableName",
		PARAM_PREFIX_encodedRegionName_ = "encodedRegionName_",
		PARAM_destinationServerName = "destinationServerName",
		PARAM_maxFileSizeMb = "maxFileSizeMb",
		PARAM_memstoreFlushSizeMb = "memstoreFlushSizeMb";

	/******************** fields ************************/

	// injected
	@Inject
	private HBaseCompactionInfo compactionInfo;
	@Inject
	private Datarouter datarouter;
	@Inject
	private HBaseBalancerFactory balancerFactory;

	// not injected
	private RouterParams<HBaseClientImp> routerParams;
	private int numRegions;
	private List<String> encodedRegionNameStrings;
	private DrServerList drServerList;
	private DrRegionList regionList;
	private Mav mav;
	private Configuration hbaseConfig = null;


	/*********** Useful methods ********************/

	private Mav initialize(){
		mav = new Mav();
		routerParams = new RouterParams<>(datarouter, params, HBASE_NEEDS);
		mav.put(RoutersHandler.PARAM_routerName, routerParams.getRouterName());
		mav.put(RoutersHandler.PARAM_clientName, routerParams.getClientName());
		mav.put(RoutersHandler.PARAM_tableName, routerParams.getTableName());
		if(routerParams.getClient() != null){
			hbaseConfig = routerParams.getClient().getHBaseConfiguration();
		}
		initializeHBaseParameters();
		return mav;
	}

	private void initializeHBaseParameters(){
		// TODO don't tie to a specific table
		encodedRegionNameStrings = RequestTool.getCheckedBoxes(request, PARAM_PREFIX_encodedRegionName_);
		numRegions = DrCollectionTool.size(encodedRegionNameStrings);
		drServerList = new DrServerList(hbaseConfig);
		if(routerParams.getNode() != null){
			regionList = new DrRegionList(routerParams.getClient(), drServerList, routerParams.getTableName(),
					hbaseConfig, routerParams.getNode(), balancerFactory.getBalancerForTable(routerParams
					.getTableName()), compactionInfo);
		}
	}


	/********************** View Handlers ***************/

	@Handler
	private Mav inspectClient(){
		initialize();
		if(routerParams.getClient() == null){
			return new MessageMav("Client not found");
		}

		mav.setViewName(PATH_JSP_HBASE + "/hbaseClientSummary.jsp");
		mav.put("address", hbaseConfig.get(HConstants.ZOOKEEPER_QUORUM));
		List<HTableDescriptor> tables;
		try{
			tables = DrListTool.create(routerParams.getClient().getAdmin().listTables());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Map<String,Map<String,String>> tableSummaryByName = new TreeMap<>();
		Map<String,Map<String,Map<String,String>>> familySummaryByTableName = new TreeMap<>();
		List<String> tableNamesForClient = routerParams.getNodes().getTableNamesForRouterAndClient(routerParams
				.getRouterName(), routerParams.getClientName());
		for(HTableDescriptor table : DrIterableTool.nullSafe(tables)){
			String tableName = table.getNameAsString();
			if(!DrCollectionTool.nullSafe(tableNamesForClient).contains(tableName)){
				continue;
			}
			Map<String,String> tableAttributeByName = new TreeMap<>();
			tableAttributeByName.put("maxFileSize", table.getMaxFileSize() + "");
			tableAttributeByName.put("memStoreFlushSize", table.getMemStoreFlushSize() + "");
			tableAttributeByName.put("readOnly", table.isReadOnly() + "");
			tableSummaryByName.put(tableName, tableAttributeByName);
			Map<String,Map<String,String>> familyAttributeByNameByFamilyName = parseTableAttributeMap(table
					.getFamilies());
			familySummaryByTableName.put(table.getNameAsString(), familyAttributeByNameByFamilyName);
			logger.warn(familySummaryByTableName.toString());
		}
		mav.put("tableSummaryByName", tableSummaryByName);
		mav.put("familySummaryByTableName", familySummaryByTableName);
		return mav;
	}

	@Handler
	private Mav viewHBaseServers() throws IOException{
		initialize();
		mav.setViewName(PATH_JSP_HBASE + "hbaseServers.jsp");
		ClusterStatus clusterStatus = routerParams.getClient().getAdmin().getClusterStatus();
		mav.put("clusterStatus", clusterStatus);
		Collection<ServerName> serverNames = new TreeSet<>(clusterStatus.getServers());
		List<DrServerInfo> servers = new ArrayList<>();
		for(ServerName serverName : DrIterableTool.nullSafe(serverNames)){
			ServerLoad serverLoad = clusterStatus.getLoad(serverName);
			servers.add(new DrServerInfo(serverName, serverLoad));
		}
		mav.put("servers", servers);
		return mav;
	}

	@Handler
	private Mav viewHBaseTableSettings(){
		initialize();
		mav.setViewName(PATH_JSP_HBASE + "hbaseTableSettings.jsp");

		HTableDescriptor table;
		try{
			table = routerParams.getClient().getAdmin().getTableDescriptor(TableName.valueOf(routerParams
					.getTableName()));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		// table level settings
		Map<String,String> tableParamByName = new TreeMap<>();
		tableParamByName.put(HBASE_TABLE_PARAM_MAX_FILESIZE, table.getMaxFileSize() / 1024 / 1024 + "");
		tableParamByName.put(HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE, table.getMemStoreFlushSize() / 1024 / 1024 + "");
		mav.put("tableParamByName", tableParamByName);

		// column family level settings
		List<HColumnDescriptor> columnFamilies = DrListTool.create(table.getColumnFamilies());
		Map<String,Map<String,String>> columnSummaryByName = new TreeMap<>();
		for(HColumnDescriptor column : DrIterableTool.nullSafe(columnFamilies)){
			Map<String,String> attributeByName = parseFamilyAttributeMap(column.getValues());
			columnSummaryByName.put(column.getNameAsString(), attributeByName);
		}
		mav.put("columnSummaryByName", columnSummaryByName);

		mav.put("compressionOptions", DrTableSettings.COMPRESSION_STRINGS);
		mav.put("dataBlockEncodingOptions", DrTableSettings.DATA_BLOCK_ENCODING_STRINGS);
		mav.put("bloomOptions", DrTableSettings.BLOOMFILTER_STRINGS);
		return mav;
	}

	@Handler
	private Mav viewHBaseTableRegions(){
		initialize();
		mav = new Mav(PATH_JSP_HBASE + "hbaseTableRegions.jsp");
		String groupBy = params.optional("groupBy").orElse("all");

		mav.put("regionsByGroup", regionList.getRegionsGroupedBy(groupBy));
		DrServerList serverList = new DrServerList(hbaseConfig);
		mav.put("serverNames", serverList.getServerNames());
		return mav;
	}


	/********************** Update Handlers ***************/

	@Handler
	private Mav updateHBaseTableAttribute(){
		initialize();
		Admin admin = routerParams.getClient().getAdmin();
		HTableDescriptor table;
		try{
			table = admin.getTableDescriptor(TableName.valueOf(routerParams.getTableName().getBytes()));
		}catch(IllegalArgumentException | IOException e){
			throw new RuntimeException(e);
		}
		try{
			admin.disableTable(TableName.valueOf(routerParams.getTableName()));
			logger.warn("table disabled");
			params.optionalLong(PARAM_maxFileSizeMb)
					.map(maxFileSizeMb -> maxFileSizeMb * 1024 * 1024)
					.ifPresent(table::setMaxFileSize);
			params.optionalLong(PARAM_memstoreFlushSizeMb)
					.map(memstoreFlushSizeMb -> memstoreFlushSizeMb * 1024 * 1024)
					.ifPresent(table::setMemStoreFlushSize);
			admin.modifyTable(TableName.valueOf(StringByteTool.getUtf8Bytes(routerParams.getTableName())), table);
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			try{
				admin.enableTable(TableName.valueOf(routerParams.getTableName()));
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}
		logger.warn("table enabled");
		mav = new MessageMav("HBase table attributes updated");
		return mav;

	}

	@Handler
	private Mav updateHBaseColumnAttribute(){
		initialize();
		Admin admin = routerParams.getClient().getAdmin();
		HTableDescriptor table;
		try{
			table = admin.getTableDescriptor(TableName.valueOf(routerParams.getTableName()));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		String columnName = params.required(RoutersHandler.PARAM_columnName);
		HColumnDescriptor column = table.getFamily(columnName.getBytes());
		try{
			// validate all settings before disabling table
			for(String colParam : DrIterableTool.nullSafe(DrTableSettings.COLUMN_SETTINGS)){
				String value = params.required(colParam);
				DrTableSettings.validateColumnFamilySetting(colParam, value);
			}
			admin.disableTable(TableName.valueOf(routerParams.getTableName()));
			logger.warn("table disabled");
			for(String colParam : DrIterableTool.nullSafe(DrTableSettings.COLUMN_SETTINGS)){
				String value = params.required(colParam);
				column.setValue(colParam, value.trim());
			}
			admin.modifyColumn(TableName.valueOf(routerParams.getTableName()), column);
		}catch(Exception e){
			logger.warn("", e);
		}finally{
			try{
				admin.enableTable(TableName.valueOf(routerParams.getTableName()));
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}
		// initializeMav();
		mav = new MessageMav("HBase column attributes updated");
		return mav;

	}


	/********************** Move Handlers
	 * @throws IOException ***************/

	@Handler
	private Mav moveRegionsToCorrectServer() throws IOException{
		int pauseBetweenRegionsMs = params.optionalInteger("pauseBetweenRegionsMs", 500);
		initialize();
		int counter = 0;
		for(DrRegionInfo<?> region : regionList.getRegions()){
			if(!region.isOnCorrectServer()){
				++counter;
				PhaseTimer timer = new PhaseTimer("move " + counter + " of " + routerParams.getTableName());
				String encodedRegionNameString = region.getRegion().getEncodedName();
				String destinationServer = region.getConsistentHashServerName().getServerName();
				routerParams.getClient().getAdmin().move(Bytes.toBytes(encodedRegionNameString),
						Bytes.toBytes(destinationServer));
				logger.warn(timer.add("HBase moved region " + encodedRegionNameString + " to server "
						+ destinationServer).toString());
			}
			ThreadTool.sleep(pauseBetweenRegionsMs);
		}

		// mav.put("message-update", "HBase regions moved to correct server");
		return new MessageMav("HBase regions moved to correct server");
	}

	@Handler
	private Mav moveHBaseTableRegions() throws IOException{
		initialize();
		String tableName = params.required(PARAM_tableName);
		String destinationServer = params.required(PARAM_destinationServerName);
		ServerName serverName = ServerNameTool.create(destinationServer);
		if(DrCollectionTool.doesNotContain(drServerList.getServerNames(), serverName)){
			throw new IllegalArgumentException(serverName + " not found");
		}
		for(int i = 0; i < numRegions; ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("move " + i + "/" + numRegions + " of " + tableName);
			routerParams.getClient().getAdmin().move(Bytes.toBytes(encodedRegionNameString), Bytes.toBytes(
					destinationServer));
			logger.warn(timer.add("HBase moved region " + encodedRegionNameString + " to server "
					+ serverName).toString());
		}
		return new MessageMav("moved regions:" + encodedRegionNameStrings);
	}


	/********************** Compact Handlers ***************/

	@Handler
	private Mav compactHBaseTableRegions(){
		initialize();
		for(int i = 0; i < encodedRegionNameStrings.size(); ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			DrRegionInfo<?> region = regionList.getRegionByEncodedName(encodedRegionNameString);
			try{
				routerParams.getClient().getAdmin().compactRegion(region.getRegion().getRegionName());
			}catch(IOException e){
				throw new RuntimeException(e);
			}

		}
		return new MessageMav("compactions requested for regions:" + encodedRegionNameStrings);
	}

	@Handler
	private Mav compactAllHBaseTableRegions(){
		initialize();
		try{
			routerParams.getClient().getAdmin().compact(TableName.valueOf(routerParams.getTableName()));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return new MessageMav("Submitted compact request for entire table " + routerParams.getTableName());
	}

	@Handler
	private Mav majorCompactHBaseTableRegions(){
		initialize();
		for(int i = 0; i < encodedRegionNameStrings.size(); ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			DrRegionInfo<?> region = regionList.getRegionByEncodedName(encodedRegionNameString);
			try{
				routerParams.getClient().getAdmin().majorCompactRegion(region.getRegion().getRegionName());
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}
		return new MessageMav("submitted major compaction requests for regions: " + encodedRegionNameStrings);
	}

	@Handler
	private Mav majorCompactAllHBaseTableRegions(){
		initialize();
		try{
			routerParams.getClient().getAdmin().majorCompact(TableName.valueOf(routerParams.getTableName()));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return new MessageMav("Submitted majorCompact request for entire table " + routerParams.getTableName());
	}


	/********************** Flush Handlers ***************/

	@Handler
	private Mav flushHBaseTableRegions(){
		initialize();
		for(int i = 0; i < encodedRegionNameStrings.size(); ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			DrRegionInfo<?> region = regionList.getRegionByEncodedName(encodedRegionNameString);
			try{
				routerParams.getClient().getAdmin().flushRegion(region.getRegion().getRegionName());
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}
		return new MessageMav("flushes requested for regions: " + encodedRegionNameStrings);
	}

	@Handler
	private Mav flushAllHBaseTableRegions(){
		initialize();
		try{
			routerParams.getClient().getAdmin().flush(TableName.valueOf(routerParams.getTableName()));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return new MessageMav("Flushed all table regions ");
	}


	/********************** Merge Handlers ***************/

	private static Map<String,Map<String,String>> parseTableAttributeMap(Collection<HColumnDescriptor> families){
		Map<String,Map<String,String>> familyAttributeByNameByFamilyName = new TreeMap<>();
		for(HColumnDescriptor family : DrIterableTool.nullSafe(families)){
			Map<String,String> familyAttributeByName = new TreeMap<>();
			familyAttributeByNameByFamilyName.put(family.getNameAsString(), familyAttributeByName);
			for(Map.Entry<ImmutableBytesWritable,ImmutableBytesWritable> e : family.getValues().entrySet()){
				String key = Bytes.toString(e.getKey().get());
				String value = Bytes.toString(e.getValue().get());
				familyAttributeByName.put(key, value);
			}
		}
		return familyAttributeByNameByFamilyName;
	}

	private static Map<String,String> parseFamilyAttributeMap(Map<ImmutableBytesWritable,ImmutableBytesWritable> ins){
		Map<String,String> outs = new TreeMap<>();
		for(Map.Entry<ImmutableBytesWritable,ImmutableBytesWritable> entry : DrMapTool.nullSafe(ins).entrySet()){
			outs.put(StringByteTool.fromUtf8Bytes(entry.getKey().get()), StringByteTool.fromUtf8Bytes(entry.getValue()
					.get()));
		}
		if(!outs.containsKey(DrTableSettings.DATA_BLOCK_ENCODING)){
			outs.put(DrTableSettings.DATA_BLOCK_ENCODING, DrTableSettings.DEFAULT_DATA_BLOCK_ENCODING);
		}
		if(!outs.containsKey(DrTableSettings.ENCODE_ON_DISK)){
			outs.put(DrTableSettings.ENCODE_ON_DISK, DrTableSettings.DEFAULT_ENCODE_ON_DISK);
		}
		return outs;
	}

	private static String
			ACTION_copyHBaseTable = "copyHBaseTable",
			ACTION_exportNodeToHFile = "exportNodeToHFile",
			ACTION_viewHBaseServers = "viewHBaseServers",
			ACTION_viewHBaseTableRegions = "viewHBaseTableRegions",
			ACTION_moveRegionsToCorrectServer = "moveRegionsToCorrectServer",
			ACTION_moveHBaseTableRegions = "moveHBaseTableRegions",
			ACTION_compactHBaseTableRegions = "compactHBaseTableRegions",
			ACTION_compactAllHBaseTableRegions = "compactAllHBaseTableRegions",
			ACTION_majorCompactHBaseTableRegions = "majorCompactHBaseTableRegions",
			ACTION_majorCompactAllHBaseTableRegions = "majorCompactAllHBaseTableRegions",
			ACTION_flushHBaseTableRegions = "flushHBaseTableRegions",
			ACTION_flushAllHBaseTableRegions = "flushAllHBaseTableRegions",
			ACTION_mergeFollowingHBaseTableRegions = "mergeFollowingHBaseTableRegions",
			ACTION_viewHBaseTableSettings = "viewHBaseTableSettings",
			ACTION_updateHBaseTableAttribute = "updateHBaseTableAttribute",
			ACTION_updateHBaseColumnAttribute = "updateHBaseColumnAttribute";

	private static final List<String> NEEDS_CLIENT = new ArrayList<>();
	static{
		NEEDS_CLIENT.add(RoutersHandler.ACTION_inspectClient);
		NEEDS_CLIENT.add(ACTION_moveRegionsToCorrectServer);
		NEEDS_CLIENT.add(ACTION_moveHBaseTableRegions);
		NEEDS_CLIENT.add(ACTION_compactHBaseTableRegions);
		NEEDS_CLIENT.add(ACTION_compactAllHBaseTableRegions);
		NEEDS_CLIENT.add(ACTION_majorCompactHBaseTableRegions);
		NEEDS_CLIENT.add(ACTION_majorCompactAllHBaseTableRegions);
		NEEDS_CLIENT.add(ACTION_flushHBaseTableRegions);
		NEEDS_CLIENT.add(ACTION_flushAllHBaseTableRegions);
		NEEDS_CLIENT.add(ACTION_mergeFollowingHBaseTableRegions);
		NEEDS_CLIENT.add(ACTION_viewHBaseTableSettings);
		NEEDS_CLIENT.add(ACTION_updateHBaseTableAttribute);
		NEEDS_CLIENT.add(ACTION_updateHBaseColumnAttribute);
		NEEDS_CLIENT.add(ACTION_viewHBaseServers);
		NEEDS_CLIENT.add(ACTION_viewHBaseTableRegions);

	}

	private static final List<String> NEEDS_ROUTER = new ArrayList<>();
	static{
		NEEDS_ROUTER.addAll(NEEDS_CLIENT);
		NEEDS_ROUTER.add(RoutersHandler.ACTION_inspectRouter);
		NEEDS_ROUTER.add(ACTION_copyHBaseTable);
		NEEDS_ROUTER.add(ACTION_exportNodeToHFile);
	}

	private static final List<String> NEEDS_NODE = new ArrayList<>();
	static{
		NEEDS_NODE.add(ACTION_copyHBaseTable);
		NEEDS_NODE.add(ACTION_exportNodeToHFile);
		NEEDS_NODE.add(ACTION_compactHBaseTableRegions);
		NEEDS_NODE.add(ACTION_flushHBaseTableRegions);
		NEEDS_NODE.add(ACTION_majorCompactHBaseTableRegions);
		NEEDS_NODE.add(ACTION_mergeFollowingHBaseTableRegions);
		NEEDS_NODE.add(ACTION_moveRegionsToCorrectServer);
		NEEDS_NODE.add(ACTION_viewHBaseTableRegions);
	}

	private static final HashMap<String,List<String>> HBASE_NEEDS = new HashMap<>();
	static{
		HBASE_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
		HBASE_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		HBASE_NEEDS.put(RouterParams.NEEDS_NODE, NEEDS_NODE);
	}

	private static final String
	// also hard-coded in hbaseTableSettings.jsp
			HBASE_TABLE_PARAM_MAX_FILESIZE = "MAX_FILESIZE",
			HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE = "MEMSTORE_FLUSHSIZE";

	private static final List<String> HBASE_TABLE_PARAMS = new ArrayList<>();
	static{
		HBASE_TABLE_PARAMS.add(HBASE_TABLE_PARAM_MAX_FILESIZE);
		HBASE_TABLE_PARAMS.add(HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE);
	}

}

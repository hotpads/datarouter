package com.hotpads.handler.admin.client.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.UnknownRegionException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.ipc.HMasterInterface;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Merge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.balancer.HBaseBalancerFactory;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHTableSettings;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionInfo;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.concurrent.ThreadTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseHandler extends BaseHandler {
	protected Logger logger = LoggerFactory.getLogger(getClass());

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
	protected DRHCompactionInfo drhCompactionInfo;
	@Inject
	private DatarouterContext dataRouterContext;
	@Inject
	private HBaseBalancerFactory balancerFactory;

	// not injected
	private static final String PATH_JSP_HBASE = "/jsp/admin/datarouter/hbase/";
	private RouterParams<HBaseClientImp> routerParams;

	protected int numRegions;
	protected List<String> encodedRegionNameStrings;
	protected DRHServerList drhServerList;
	protected DRHRegionList regionList;
	private Mav mav;

	private Configuration hbaseConfig = null;
//	private Node<?,?> node = null;

	/*********** Useful methods ********************/

	private Mav initialize(){
		mav = new Mav();
		routerParams = new RouterParams<HBaseClientImp>(dataRouterContext, params, HBASE_NEEDS);
		// mav.put(RequestTool.SUBMIT_ACTION, ACTION_viewHBaseTableRegions);
		mav.put(RoutersHandler.PARAM_routerName, routerParams.getRouterName());
		mav.put(RoutersHandler.PARAM_clientName, routerParams.getClientName());
		mav.put(RoutersHandler.PARAM_tableName, routerParams.getTableName());
		if(routerParams.getClient() != null){
			hbaseConfig = routerParams.getClient().getHBaseConfiguration();
		}
		// Is it required ?
		// RoutersHandler.initClients(ListTool.create(paramsRouter.getRouter()));
		initializeHBaseParameters();
		return mav;
	}

	private void initializeHBaseParameters(){
		// TODO don't tie to a specific table
		encodedRegionNameStrings = RequestTool.getCheckedBoxes(request, PARAM_PREFIX_encodedRegionName_);
		numRegions = CollectionTool.size(encodedRegionNameStrings);
		drhServerList = new DRHServerList(hbaseConfig);
		if(routerParams.getNode() != null){
			regionList = new DRHRegionList(routerParams.getClient(), drhServerList, routerParams.getTableName(),
					hbaseConfig, routerParams.getNode(), balancerFactory.getBalancerForTable(routerParams.getTableName()),
					drhCompactionInfo);
		}
	}

	/****************************************************/
	/********************** View Handlers ***************/
	/****************************************************/

	@Handler
	protected Mav inspectClient(){
		initialize();
		if(routerParams.getClient() == null){ return new MessageMav("Client not found"); }

		mav.setViewName(PATH_JSP_HBASE + "/hbaseClientSummary.jsp");
		mav.put("address", hbaseConfig.get(HConstants.ZOOKEEPER_QUORUM));
		List<HTableDescriptor> tables = null;
		try{
			tables = ListTool.create(routerParams.getClient().getHBaseAdmin().listTables());
		}catch(IOException e){
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		Map<String,Map<String,String>> tableSummaryByName = MapTool.createTreeMap();
		@SuppressWarnings("unchecked") List<String> tableNamesForClient = routerParams.getNodes()
				.getTableNamesForRouterAndClient(routerParams.getRouterName(), routerParams.getClientName());
		for(HTableDescriptor table : IterableTool.nullSafe(tables)){
			String tableName = table.getNameAsString();
			if(!CollectionTool.nullSafe(tableNamesForClient).contains(tableName)){
				continue;
			}
			Map<String,String> attributeByName = parseFamilyAttributeMap(table.getValues());
			attributeByName.put("maxFileSize", table.getMaxFileSize() + "");
			attributeByName.put("memStoreFlushSize", table.getMemStoreFlushSize() + "");
			attributeByName.put("readOnly", table.isReadOnly() + "");
			tableSummaryByName.put(tableName, attributeByName);
		}
		mav.put("tableSummaryByName", tableSummaryByName);
		return mav;
	}

	@Handler
	protected Mav viewHBaseServers() {
		initialize();
		mav.setViewName(PATH_JSP_HBASE + "hbaseServers.jsp");
		HMasterInterface master = null;
		try {
			master = routerParams.getClient().getHBaseAdmin().getMaster();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (master != null) {
			ClusterStatus clusterStatus = master.getClusterStatus();
			mav.put("clusterStatus", clusterStatus);
			Collection<ServerName> serverNames = clusterStatus.getServers();
			List<DRHServerInfo> servers = ListTool.create();
			for (ServerName serverName : IterableTool.nullSafe(serverNames)) {
				HServerLoad hServerLoad = clusterStatus.getLoad(serverName);
				servers.add(new DRHServerInfo(serverName, hServerLoad));
			}
			mav.put("servers", servers);
		}
		return mav;
	}

	@Handler
	protected Mav viewHBaseTableSettings(){
		initialize();
		mav.setViewName(PATH_JSP_HBASE + "hbaseTableSettings.jsp");

		HTableDescriptor table = null;
		try{
			table = routerParams.getClient().getHBaseAdmin().getTableDescriptor(routerParams.getTableName().getBytes());
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		if(table != null){
			// table level settings
			Map<String,String> tableParamByName = MapTool.createTreeMap();
			tableParamByName.put(HBASE_TABLE_PARAM_MAX_FILESIZE, table.getMaxFileSize() / 1024 / 1024 + "");
			tableParamByName.put(HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE, table.getMemStoreFlushSize() / 1024 / 1024 + "");
			mav.put("tableParamByName", tableParamByName);

			// column family level settings
			List<HColumnDescriptor> columnFamilies = ListTool.create(table.getColumnFamilies());
			Map<String,Map<String,String>> columnSummaryByName = MapTool.createTreeMap();
			for(HColumnDescriptor column : IterableTool.nullSafe(columnFamilies)){
				Map<String,String> attributeByName = parseFamilyAttributeMap(column.getValues());
				columnSummaryByName.put(column.getNameAsString(), attributeByName);
			}
			mav.put("columnSummaryByName", columnSummaryByName);
		}

		return mav;

	}

	@Handler
	protected Mav viewHBaseTableRegions(){
		initialize();
		mav = new Mav(PATH_JSP_HBASE + "hbaseTableRegions.jsp");
		String groupBy = RequestTool.get(request, "groupBy", null);

		mav.put("regionsByGroup", regionList.getRegionsGroupedBy(groupBy));
		DRHServerList serverList = new DRHServerList(hbaseConfig);
		mav.put("serverNames", serverList.getServerNames());
		return mav;
	}

	/****************************************************/
	/********************** Update Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav updateHBaseTableAttribute(){
		initialize();
		HBaseAdmin admin = routerParams.getClient().getHBaseAdmin();
		HTableDescriptor table = null;
		try{
			table = admin.getTableDescriptor(routerParams.getTableName().getBytes());

			if(table != null){
				try{
					admin.disableTable(routerParams.getTableName());
					logger.warn("table disabled");
					Long maxFileSizeMb = RequestTool.getLong(request, PARAM_maxFileSizeMb, null);
					if(maxFileSizeMb != null){
						table.setMaxFileSize(maxFileSizeMb * 1024 * 1024);
					}
					Long memstoreFlushSizeMb = RequestTool.getLong(request, PARAM_memstoreFlushSizeMb,
							null);
					if(memstoreFlushSizeMb != null){
						table.setMemStoreFlushSize(memstoreFlushSizeMb * 1024 * 1024);
					}
					admin.modifyTable(StringByteTool.getUtf8Bytes(routerParams.getTableName()), table);
				}catch(Exception e){
					logger.warn("", e);
				}finally{
					admin.enableTable(routerParams.getTableName());
				}
				logger.warn("table enabled");

			}
		}catch(TableNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e1){
			e1.printStackTrace();
		}
		// initializeMav();
		mav = new MessageMav("HBase table attributes updated");
		return mav;

	}

	@Handler
	protected Mav updateHBaseColumnAttribute(){
		initialize();
		HBaseAdmin admin = routerParams.getClient().getHBaseAdmin();
		HTableDescriptor table;
		try{
			table = admin.getTableDescriptor(routerParams.getTableName().getBytes());

			String columnName = RequestTool.get(request, RoutersHandler.PARAM_columnName);
			HColumnDescriptor column = table.getFamily(columnName.getBytes());
			try{
				// validate all settings before disabling table
				for(String colParam : IterableTool.nullSafe(DRHTableSettings.COLUMN_SETTINGS)){
					String value = RequestTool.get(request, colParam);
					DRHTableSettings.validateColumnFamilySetting(colParam, value);
				}
				admin.disableTable(routerParams.getTableName());
				logger.warn("table disabled");
				for(String colParam : IterableTool.nullSafe(DRHTableSettings.COLUMN_SETTINGS)){
					String value = RequestTool.get(request, colParam);
					column.setValue(colParam, value.trim());
				}
				admin.modifyColumn(routerParams.getTableName(), column);
			}catch(Exception e){
				logger.warn("", e);
			}finally{
				admin.enableTable(routerParams.getTableName());
			}
		}catch(TableNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e1){
			e1.printStackTrace();
		}
		// initializeMav();
		mav = new MessageMav("HBase column attributes updated");
		return mav;

	}

	/****************************************************/
	/********************** Move Handlers ***************/
	/****************************************************/

	@Handler
	protected Mav moveRegionsToCorrectServer(){
		int pauseBetweenRegionsMs = params.optionalInteger("pauseBetweenRegionsMs", 500);
		initialize();
		int counter = 0;
		for(DRHRegionInfo<?> region : regionList.getRegions()){
			if(!region.isOnCorrectServer()){
				++counter;
				PhaseTimer timer = new PhaseTimer("move " + counter + " of " + routerParams.getTableName());
				String encodedRegionNameString = region.getRegion().getEncodedName();
				String destinationServer = region.getConsistentHashServerName().getServerName();
				try{
					routerParams.getClient().getHBaseAdmin().move(Bytes.toBytes(encodedRegionNameString),
							Bytes.toBytes(destinationServer));
				}catch(UnknownRegionException | MasterNotRunningException | ZooKeeperConnectionException e){
					throw new RuntimeException(e);
				}
				logger.warn(timer.add("HBase moved region " + encodedRegionNameString + " to server "
						+ destinationServer).toString());
			}
			ThreadTool.sleep(pauseBetweenRegionsMs);
		}

		// mav.put("message-update", "HBase regions moved to correct server");
		return new MessageMav("HBase regions moved to correct server");
	}

	@Handler
	protected Mav moveHBaseTableRegions() {
		initialize();
		String tableName = params.required(PARAM_tableName);
		String destinationServer = params.required(PARAM_destinationServerName);
		ServerName serverName = new ServerName(destinationServer);
		if(CollectionTool.doesNotContain(drhServerList.getServerNames(), serverName)){ 
			throw new IllegalArgumentException(serverName + " not found"); 
		}
		for(int i = 0; i < numRegions; ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("move " + i + "/" + numRegions + " of " + tableName);
			try{
				routerParams.getClient().getHBaseAdmin().move(Bytes.toBytes(encodedRegionNameString), Bytes.toBytes(
						destinationServer));
			}catch(UnknownRegionException | MasterNotRunningException | ZooKeeperConnectionException e){
				throw new RuntimeException(e);
			}
			logger.warn(timer.add("HBase moved region " + encodedRegionNameString + " to server "
					+ serverName).toString());
		}
		return new MessageMav("moved regions:"+encodedRegionNameStrings);
	}

	/****************************************************/
	/********************** Compact Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav compactHBaseTableRegions(){
		initialize();
		for(int i = 0; i < encodedRegionNameStrings.size(); ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			DRHRegionInfo<?> region = regionList.getRegionByEncodedName(encodedRegionNameString);
			try{
				routerParams.getClient().getHBaseAdmin().compact(region.getRegion().getRegionName());
			}catch(IOException | InterruptedException e){
				throw new RuntimeException(e);
			}

		}
		return new MessageMav("compactions requested for regions:"+encodedRegionNameStrings);
	}

	@Handler
	protected Mav compactAllHBaseTableRegions(){
		initialize();
		try{
			routerParams.getClient().getHBaseAdmin().compact(routerParams.getTableName());
		}catch(IOException | InterruptedException e){
			throw new RuntimeException(e);
		}
		return new MessageMav("Submitted compact request for entire table " + routerParams.getTableName());
	}

	@Handler
	protected Mav majorCompactHBaseTableRegions(){
		initialize();
		for(int i = 0; i < encodedRegionNameStrings.size(); ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			DRHRegionInfo<?> region = regionList.getRegionByEncodedName(encodedRegionNameString);
			try{
				routerParams.getClient().getHBaseAdmin().majorCompact(region.getRegion().getRegionName());
			}catch(IOException | InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		return new MessageMav("submitted major compaction requests for regions: "+encodedRegionNameStrings);
	}

	@Handler
	protected Mav majorCompactAllHBaseTableRegions(){
		initialize();
		try{
			routerParams.getClient().getHBaseAdmin().majorCompact(routerParams.getTableName());
		}catch(IOException | InterruptedException e){
			throw new RuntimeException(e);
		}
		return new MessageMav("Submitted majorCompact request for entire table " + routerParams.getTableName());
	}

	/****************************************************/
	/********************** Flush Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav flushHBaseTableRegions() {
		initialize();
		for(int i = 0; i < encodedRegionNameStrings.size(); ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("flush " + i + "/" + numRegions + " on " + routerParams.getTableName());
			DRHRegionInfo<?> region = regionList.getRegionByEncodedName(encodedRegionNameString);
			try{
				routerParams.getClient().getHBaseAdmin().flush(region.getRegion().getRegionName());
			}catch(IOException | InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		return new MessageMav("flushes requested for regions: "+encodedRegionNameStrings);
	}

	@Handler
	protected Mav flushAllHBaseTableRegions(){
		initialize();
		try{
			routerParams.getClient().getHBaseAdmin().flush(routerParams.getTableName());
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return new MessageMav("Flushed all table regions ");
	}

	/****************************************************/
	/********************** Merge Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav mergeFollowingHBaseTableRegions() {
		initialize();
		Merge merge = new Merge(hbaseConfig);
		for (int i = 0; i < encodedRegionNameStrings.size(); ++i) {
			PhaseTimer timer = new PhaseTimer("merge " + i + "/" + numRegions + " on " + routerParams.getTableName());
			DRHRegionInfo<?> regionA = regionList.getRegionByEncodedName(encodedRegionNameStrings.get(i));
			if (regionA == null) {
				logger.warn(timer.add("couldn't find " + routerParams.getTableName() + " region "
						+ encodedRegionNameStrings.get(i)).toString());
				continue;
			}
			DRHRegionInfo<?> regionB = regionList.getRegionAfter(encodedRegionNameStrings.get(i));
			try {
				routerParams.getClient().getHBaseAdmin().flush(regionA.getRegion().getRegionName());
				routerParams.getClient().getHBaseAdmin().flush(regionB.getRegion().getRegionName());
				merge.run(new String[] { routerParams.getTableName(),
						regionA.getRegion().getRegionNameAsString(),
						regionB.getRegion().getRegionNameAsString() });
				logger.warn(timer.add("merged "
						+ regionA.getRegion().getRegionNameAsString() + " and "
						+ regionB.getRegion().getRegionNameAsString()).toString());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return new MessageMav("Merged HBase table regions ");
	}

	private static Map<String,String> parseFamilyAttributeMap(Map<ImmutableBytesWritable,ImmutableBytesWritable> ins){
		Map<String,String> outs = MapTool.createTreeMap();
		for(Map.Entry<ImmutableBytesWritable,ImmutableBytesWritable> entry : MapTool.nullSafe(ins).entrySet()){
			outs.put(StringByteTool.fromUtf8Bytes(entry.getKey().get()), StringByteTool.fromUtf8Bytes(entry.getValue()
					.get()));
		}
		if(!outs.containsKey(DRHTableSettings.DATA_BLOCK_ENCODING)){
			outs.put(DRHTableSettings.DATA_BLOCK_ENCODING, DRHTableSettings.DEFAULT_DATA_BLOCK_ENCODING);
		}
		if(!outs.containsKey(DRHTableSettings.ENCODE_ON_DISK)){
			outs.put(DRHTableSettings.ENCODE_ON_DISK, DRHTableSettings.DEFAULT_ENCODE_ON_DISK);
		}
		return outs;
	}

	protected static String ACTION_countHBaseTableCells = "countHBaseTableCells",
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

	private static final List<String> NEEDS_CLIENT = ListTool.create();
	static {
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

	private static final List<String> NEEDS_ROUTER = ListTool.create();
	static {
		NEEDS_ROUTER.addAll(NEEDS_CLIENT);
		NEEDS_ROUTER.add(RoutersHandler.ACTION_inspectRouter);
		NEEDS_ROUTER.add(ACTION_copyHBaseTable);
		NEEDS_ROUTER.add(ACTION_exportNodeToHFile);
	}

	private static final List<String> NEEDS_NODE = ListTool.create();
	static {
		NEEDS_NODE.add(ACTION_copyHBaseTable);
		NEEDS_NODE.add(ACTION_exportNodeToHFile);
		NEEDS_NODE.add(ACTION_compactHBaseTableRegions);
		NEEDS_NODE.add(ACTION_flushHBaseTableRegions);
		NEEDS_NODE.add(ACTION_majorCompactHBaseTableRegions);
		NEEDS_NODE.add(ACTION_mergeFollowingHBaseTableRegions);
		NEEDS_NODE.add(ACTION_moveRegionsToCorrectServer);
		NEEDS_NODE.add(ACTION_viewHBaseTableRegions);
	}

	private static final HashMap<String, List<String>> HBASE_NEEDS = MapTool
			.createHashMap();
	static {
		HBASE_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
		HBASE_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		HBASE_NEEDS.put(RouterParams.NEEDS_NODE, NEEDS_NODE);
	}

	private static final String
	// also hard-coded in hbaseTableSettings.jsp
			HBASE_TABLE_PARAM_MAX_FILESIZE = "MAX_FILESIZE",
			HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE = "MEMSTORE_FLUSHSIZE";

	private static final List<String> HBASE_TABLE_PARAMS = ListTool.create();
	static {
		HBASE_TABLE_PARAMS.add(HBASE_TABLE_PARAM_MAX_FILESIZE);
		HBASE_TABLE_PARAMS.add(HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE);
	}

}

package com.hotpads.handler.admin.client.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.balancer.BalancerStrategy;
import com.hotpads.datarouter.client.imp.hbase.balancer.imp.ConsistentHashBalancer;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHTableSettings;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionInfo;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.routing.ParamsRouter;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.BaseHandler.Handler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseHandler extends BaseHandler {
	protected Logger logger = Logger.getLogger(getClass());

	public static BalancerStrategy getBalancerStrategyForTable(String tableName) {
		return new ConsistentHashBalancer();
	}

	/******************** fields ************************/

	// injected
	@Inject
	protected DRHCompactionInfo drhCompactionInfo;
	@Inject
	private DataRouterContext dataRouterContext;

	// not injected
	private static final String PATH_JSP_HBASE = "/jsp/admin/datarouter/hbase/";
	private ParamsRouter<HBaseClientImp> paramsRouter;

	protected int numRegions;
	protected List<String> encodedRegionNameStrings;
	protected DRHServerList drhServerList;
	protected DRHRegionList regionList;
	private Mav mav;

	private Configuration hbaseConfig = null;
	private Node<?, ?> node = null;

	/*********** Useful methods ********************/

	private Mav initialize() {
		mav = new Mav();
		paramsRouter = new ParamsRouter<HBaseClientImp>(dataRouterContext,
				params, HBASE_NEEDS);
		// mav.put(RequestTool.SUBMIT_ACTION, ACTION_viewHBaseTableRegions);
		mav.put(RoutersHandler.PARAM_routerName, paramsRouter.getRouterName());
		mav.put(RoutersHandler.PARAM_clientName, paramsRouter.getClientName());
		mav.put(RoutersHandler.PARAM_tableName, paramsRouter.getTableName());
		if (paramsRouter.getClient() != null) {
			hbaseConfig = paramsRouter.getClient().getHBaseConfiguration();
		}
		// Is it required ?
		// RoutersHandler.initClients(ListTool.create(paramsRouter.getRouter()));
		return mav;
	}

	protected void initializeHBaseParameters() {
		// TODO don't tie to a specific table
		encodedRegionNameStrings = params.optionalCsvList(
				RoutersHandler.PARAM_PREFIX_encodedRegionName_, null);
		numRegions = CollectionTool.size(encodedRegionNameStrings);
		drhServerList = new DRHServerList(hbaseConfig);
		regionList = new DRHRegionList(paramsRouter.getClient(), drhServerList,
				paramsRouter.getTableName(), hbaseConfig, node,
				getBalancerStrategyForTable(paramsRouter.getTableName()),
				drhCompactionInfo);
	}

	/****************************************************/
	/********************** View Handlers ***************/
	/****************************************************/

	@Handler
	protected Mav inspectClient() {
		initialize();
		if (paramsRouter.getClient() == null) {
			return new MessageMav("Client not found");
		}

		mav.setViewName(PATH_JSP_HBASE + "/hbaseClientSummary.jsp");
		mav.put("address", hbaseConfig.get(HConstants.ZOOKEEPER_QUORUM));
		List<HTableDescriptor> tables = null;
		try {
			tables = ListTool.create(paramsRouter.getClient().getHBaseAdmin()
					.listTables());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Map<String, String>> tableSummaryByName = MapTool
				.createTreeMap();
		@SuppressWarnings("unchecked")
		List<String> tableNamesForClient = paramsRouter
				.getRouter()
				.getContext()
				.getNodes()
				.getTableNamesForRouterAndClient(paramsRouter.getRouterName(),
						paramsRouter.getClientName());
		for (HTableDescriptor table : IterableTool.nullSafe(tables)) {
			String tableName = table.getNameAsString();
			if (!CollectionTool.nullSafe(tableNamesForClient).contains(
					tableName)) {
				continue;
			}
			Map<String, String> attributeByName = parseFamilyAttributeMap(table
					.getValues());
			attributeByName.put("maxFileSize", table.getMaxFileSize() + "");
			attributeByName.put("memStoreFlushSize",
					table.getMemStoreFlushSize() + "");
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
			master = paramsRouter.getClient().getHBaseAdmin().getMaster();
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
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
	protected Mav viewHBaseTableSettings() {
		initialize();
		mav.setViewName(PATH_JSP_HBASE + "hbaseTableSettings.jsp");

		HTableDescriptor table = null;
		try {
			table = paramsRouter.getClient().getHBaseAdmin()
					.getTableDescriptor(paramsRouter.getTableName().getBytes());
		} catch (TableNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (table != null) {
			// table level settings
			Map<String, String> tableParamByName = MapTool.createTreeMap();
			tableParamByName.put(HBASE_TABLE_PARAM_MAX_FILESIZE,
					table.getMaxFileSize() / 1024 / 1024 + "");
			tableParamByName.put(HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE,
					table.getMemStoreFlushSize() / 1024 / 1024 + "");
			mav.put("tableParamByName", tableParamByName);

			// column family level settings
			List<HColumnDescriptor> columnFamilies = ListTool.create(table
					.getColumnFamilies());
			Map<String, Map<String, String>> columnSummaryByName = MapTool
					.createTreeMap();
			for (HColumnDescriptor column : IterableTool
					.nullSafe(columnFamilies)) {
				Map<String, String> attributeByName = parseFamilyAttributeMap(column
						.getValues());
				columnSummaryByName.put(column.getNameAsString(),
						attributeByName);
			}
			mav.put("columnSummaryByName", columnSummaryByName);
		}

		return mav;

	}

	@Handler
	protected Mav viewHBaseTableRegions() {
		initialize();
		initializeHBaseParameters();
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
	protected Mav updateHBaseTableAttribute() {
		initialize();
		HBaseAdmin admin = paramsRouter.getClient().getHBaseAdmin();
		HTableDescriptor table = null;
		try {
			table = admin.getTableDescriptor(paramsRouter.getTableName()
					.getBytes());

			if (table != null) {
				try {
					admin.disableTable(paramsRouter.getTableName());
					logger.warn("table disabled");
					Long maxFileSizeMb = RequestTool.getLong(request,
							RoutersHandler.PARAM_maxFileSizeMb, null);
					if (maxFileSizeMb != null) {
						table.setMaxFileSize(maxFileSizeMb * 1024 * 1024);
					}
					Long memstoreFlushSizeMb = RequestTool.getLong(request,
							RoutersHandler.PARAM_memstoreFlushSizeMb, null);
					if (memstoreFlushSizeMb != null) {
						table.setMemStoreFlushSize(memstoreFlushSizeMb * 1024 * 1024);
					}
					admin.modifyTable(StringByteTool.getUtf8Bytes(paramsRouter
							.getTableName()), table);
				} catch (Exception e) {
					logger.warn(ExceptionTool.getStackTraceAsString(e));
				} finally {
					admin.enableTable(paramsRouter.getTableName());
				}
				logger.warn("table enabled");

			}
		} catch (TableNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// initializeMav();
		mav = new MessageMav("HBase table attributes updated");
		return mav;

	}

	@Handler
	protected Mav updateHBaseColumnAttribute() {
		initialize();
		HBaseAdmin admin = paramsRouter.getClient().getHBaseAdmin();
		HTableDescriptor table;
		try {
			table = admin.getTableDescriptor(paramsRouter.getTableName()
					.getBytes());

			String columnName = RequestTool.get(request,
					RoutersHandler.PARAM_columnName);
			HColumnDescriptor column = table.getFamily(columnName.getBytes());
			try {
				// validate all settings before disabling table
				for (String colParam : IterableTool
						.nullSafe(DRHTableSettings.COLUMN_SETTINGS)) {
					String value = RequestTool.get(request, colParam);
					DRHTableSettings.validateColumnFamilySetting(colParam,
							value);
				}
				admin.disableTable(paramsRouter.getTableName());
				logger.warn("table disabled");
				for (String colParam : IterableTool
						.nullSafe(DRHTableSettings.COLUMN_SETTINGS)) {
					String value = RequestTool.get(request, colParam);
					column.setValue(colParam, value.trim());
				}
				admin.modifyColumn(paramsRouter.getTableName(), column);
			} catch (Exception e) {
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			} finally {
				admin.enableTable(paramsRouter.getTableName());
			}
		} catch (TableNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
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
	protected Mav moveRegionsToCorrectServer() {
		initialize();
		initializeHBaseParameters();
		int counter = 0;
		for (DRHRegionInfo region : regionList.getRegions()) {
			if (!region.isOnCorrectServer()) {
				++counter;
				PhaseTimer timer = new PhaseTimer("move " + counter + " of "
						+ paramsRouter.getTableName());
				String encodedRegionNameString = region.getRegion()
						.getEncodedName();
				String destinationServer = region.getConsistentHashServerName()
						.getHostname();
				try {
					paramsRouter
							.getClient()
							.getHBaseAdmin()
							.move(Bytes.toBytes(encodedRegionNameString),
									Bytes.toBytes(destinationServer));
				} catch (UnknownRegionException | MasterNotRunningException
						| ZooKeeperConnectionException e) {
					e.printStackTrace();
				}
				logger.warn(timer.add("HBase moved region "
						+ encodedRegionNameString + " to server "
						+ destinationServer));
			}
		}

		// mav.put("message-update", "HBase regions moved to correct server");
		mav = new MessageMav("HBase regions moved to correct server");
		return mav;
	}

	@Handler
	protected Mav moveHBaseTableRegions() {
		initializeHBaseParameters();

		// String destinationServer = RequestTool.get(request,
		// PARAM_destinationServerName);
		// ServerName serverName = new ServerName(destinationServer);
		// DRHServerList serverList = new DRHServerList(hbaseConfig);
		//
		// if(CollectionTool.doesNotContain(serverList.getServerNames(),
		// serverName)){ throw new IllegalArgumentException(
		// serverName + " not found"); }
		// for(int i = 0; i < numRegions; ++i){
		// String encodedRegionNameString = encodedRegionNameStrings.get(i);
		// PhaseTimer timer = new PhaseTimer("move " + i + "/" + numRegions +
		// " of " + tableName);
		// try{
		// admin.move(Bytes.toBytes(encodedRegionNameString),
		// Bytes.toBytes(destinationServer));
		// }catch(UnknownRegionException | MasterNotRunningException |
		// ZooKeeperConnectionException e){
		// e.printStackTrace();
		// }
		// logger.warn(timer.add("HBase moved region " + encodedRegionNameString
		// + " to server " + serverName));
		//
		// }
		// initializeMav();
		// mav = new MessageMav("HBase moved  several table regions to server "
		// + "");
		mav = new MessageMav("Handler not implemented ");

		return mav;
	}

	/****************************************************/
	/********************** Compact Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav compactHBaseTableRegions() {
		initialize();

		initializeHBaseParameters();
		for (int i = 0; i < numRegions; ++i) {
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("compact " + i + "/" + numRegions
					+ " on " + paramsRouter.getTableName());
			DRHRegionInfo region = regionList
					.getRegionByEncodedName(encodedRegionNameString);
			if (region == null) {
				logger.warn(timer.add("couldn't find "
						+ paramsRouter.getTableName() + " region "
						+ encodedRegionNameString));
				continue;
			}
			try {
				paramsRouter.getClient().getHBaseAdmin()
						.compact(region.getRegion().getRegionName());
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			logger.warn(timer.add("Submitted compact request region "
					+ encodedRegionNameString));

		}

		// initializeMav();
		mav = new MessageMav("HBased compacted several table regions");

		return mav;
	}

	@Handler
	protected Mav compactAllHBaseTableRegions() {
		initialize();

		initializeHBaseParameters();

		try {
			paramsRouter.getClient().getHBaseAdmin()
					.compact(paramsRouter.getTableName());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		logger.warn("Submitted compact request for entire table "
				+ paramsRouter.getTableName());
		mav = new MessageMav("Submitted compact request for entire table "
				+ paramsRouter.getTableName());

		// initializeMav();
		return mav;
	}

	@Handler
	protected Mav majorCompactHBaseTableRegions() {
		initialize();

		initializeHBaseParameters();

		for (int i = 0; i < numRegions; ++i) {
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("majorCompact " + i + "/"
					+ numRegions + " on " + paramsRouter.getTableName());
			DRHRegionInfo region = regionList
					.getRegionByEncodedName(encodedRegionNameString);
			if (region == null) {
				logger.warn(timer.add("couldn't find "
						+ paramsRouter.getTableName() + " region "
						+ encodedRegionNameString));
				continue;
			}
			try {
				paramsRouter.getClient().getHBaseAdmin()
						.majorCompact(region.getRegion().getRegionName());
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			logger.warn(timer.add("Submitted majorCompact request region "
					+ encodedRegionNameString));
			mav = new MessageMav("Submitted majorCompact request region "
					+ encodedRegionNameString);
		}

		return mav;
	}

	@Handler
	protected Mav majorCompactAllHBaseTableRegions() {
		initialize();

		initializeHBaseParameters();

		try {
			paramsRouter.getClient().getHBaseAdmin()
					.majorCompact(paramsRouter.getTableName());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		logger.warn("Submitted majorCompact request for entire table "
				+ paramsRouter.getTableName());

		// initializeMav();
		mav = new MessageMav("Submitted majorCompact request for entire table "
				+ paramsRouter.getTableName());

		return mav;
	}

	/****************************************************/
	/********************** Flush Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav flushHBaseTableRegions() {
		initialize();

		initializeHBaseParameters();

		for (int i = 0; i < numRegions; ++i) {
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("flush " + i + "/" + numRegions
					+ " on " + paramsRouter.getTableName());
			DRHRegionInfo region = regionList
					.getRegionByEncodedName(encodedRegionNameString);
			if (region == null) {
				logger.warn(timer.add("couldn't find "
						+ paramsRouter.getTableName() + " region "
						+ encodedRegionNameString));
				continue;
			}
			try {
				paramsRouter.getClient().getHBaseAdmin()
						.flush(region.getRegion().getRegionName());
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			logger.warn(timer.add("Submitted flush request region "
					+ encodedRegionNameString));
		}

		// initializeMav();
		mav = new MessageMav("Flushed several table regions ");

		return mav;
	}

	@Handler
	protected Mav flushAllHBaseTableRegions() {
		initialize();

		initializeHBaseParameters();
		try {
			paramsRouter.getClient().getHBaseAdmin()
					.flush(paramsRouter.getTableName());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		logger.warn("Submitted flush request for entire table "
				+ paramsRouter.getTableName());

		for (int i = 0; i < numRegions; ++i) {
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("flush " + i + "/" + numRegions
					+ " on " + paramsRouter.getTableName());
			DRHRegionInfo region = regionList
					.getRegionByEncodedName(encodedRegionNameString);
			if (region == null) {
				logger.warn(timer.add("couldn't find "
						+ paramsRouter.getTableName() + " region "
						+ encodedRegionNameString));
				continue;
			}
			try {
				paramsRouter.getClient().getHBaseAdmin()
						.flush(region.getRegion().getRegionName());
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			logger.warn(timer.add("Submitted flush request region "
					+ encodedRegionNameString));
		}

		mav = new MessageMav("Flushed all table regions ");

		return mav;
	}

	/****************************************************/
	/********************** Merge Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav mergeFollowingHBaseTableRegions() {
		initialize();

		initializeHBaseParameters();

		Merge merge = new Merge(hbaseConfig);
		for (int i = 0; i < numRegions; ++i) {
			PhaseTimer timer = new PhaseTimer("merge " + i + "/" + numRegions
					+ " on " + paramsRouter.getTableName());
			DRHRegionInfo regionA = regionList
					.getRegionByEncodedName(encodedRegionNameStrings.get(i));
			if (regionA == null) {
				logger.warn(timer.add("couldn't find "
						+ paramsRouter.getTableName() + " region "
						+ encodedRegionNameStrings.get(i)));
				continue;
			}
			DRHRegionInfo regionB = regionList
					.getRegionAfter(encodedRegionNameStrings.get(i));
			try {
				paramsRouter.getClient().getHBaseAdmin()
						.flush(regionA.getRegion().getRegionName());
				paramsRouter.getClient().getHBaseAdmin()
						.flush(regionB.getRegion().getRegionName());
				merge.run(new String[] { paramsRouter.getTableName(),
						regionA.getRegion().getRegionNameAsString(),
						regionB.getRegion().getRegionNameAsString() });
				logger.warn(timer.add("merged "
						+ regionA.getRegion().getRegionNameAsString() + " and "
						+ regionB.getRegion().getRegionNameAsString()));
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// initializeMav();
		mav = new MessageMav("Merged HBase table regions ");

		return mav;
	}

	private static Map<String, String> parseFamilyAttributeMap(
			Map<ImmutableBytesWritable, ImmutableBytesWritable> ins) {
		Map<String, String> outs = MapTool.createTreeMap();
		for (Map.Entry<ImmutableBytesWritable, ImmutableBytesWritable> entry : MapTool
				.nullSafe(ins).entrySet()) {
			outs.put(StringByteTool.fromUtf8Bytes(entry.getKey().get()),
					StringByteTool.fromUtf8Bytes(entry.getValue().get()));
		}
		if (!outs.containsKey(DRHTableSettings.DATA_BLOCK_ENCODING)) {
			outs.put(DRHTableSettings.DATA_BLOCK_ENCODING,
					DRHTableSettings.DEFAULT_DATA_BLOCK_ENCODING);
		}
		if (!outs.containsKey(DRHTableSettings.ENCODE_ON_DISK)) {
			outs.put(DRHTableSettings.ENCODE_ON_DISK,
					DRHTableSettings.DEFAULT_ENCODE_ON_DISK);
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
		HBASE_NEEDS.put(ParamsRouter.NEEDS_CLIENT, NEEDS_CLIENT);
		HBASE_NEEDS.put(ParamsRouter.NEEDS_ROUTER, NEEDS_ROUTER);
		HBASE_NEEDS.put(ParamsRouter.NEEDS_NODE, NEEDS_NODE);
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

package com.hotpads.handler.admin.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.UnknownRegionException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.ipc.HMasterInterface;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Merge;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.hotpads.datarouter.client.imp.hbase.balancer.BalancerStrategy;
import com.hotpads.datarouter.client.imp.hbase.balancer.imp.ConsistentHashBalancer;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHTableSettings;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionInfo;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.BaseHandler.Handler;
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

	public static BalancerStrategy getBalancerStrategyForTable(String tableName) {
		return new ConsistentHashBalancer();
	}

	
	/******************** fields ************************/
	
	//injected
	@Inject
	protected DRHCompactionInfo drhCompactionInfo;
	
	//not injected
	private static final String PATH_JSP_HBASE = "/jsp/admin/datarouter/hbase/";
	protected String tableName;
	protected HBaseAdmin admin;
	protected int numRegions;
	protected List<String> encodedRegionNameStrings;
	protected DRHServerList drhServerList;
	protected DRHRegionList regionList;


	/*********** Useful methods ********************/
	
	private Mav initializeMav() {
		mav = new Mav();
		mav = new Mav("redirect:" + request.getContextPath()
				+ "/datarouter/routers");
		// mav.put(RequestTool.SUBMIT_ACTION, ACTION_viewHBaseTableRegions);
		mav.put(PARAM_routerName, routerName);
		mav.put(PARAM_clientName, clientName);
		mav.put(PARAM_tableName, tableName);
		return mav;
	}

	protected void initializeHBaseParameters() {
		// TODO don't tie to a specific table
		tableName = RequestTool.get(request, PARAM_tableName);
		admin = hbaseClient.getHBaseAdmin();
		encodedRegionNameStrings = RequestTool.getCheckedBoxes(request,
				PARAM_PREFIX_encodedRegionName_);
		numRegions = CollectionTool.size(encodedRegionNameStrings);
		drhServerList = new DRHServerList(hbaseConfig);
		regionList = new DRHRegionList(hbaseClient, drhServerList, tableName,
				hbaseConfig, node, getBalancerStrategyForTable(tableName),
				drhCompactionInfo);
	}
	
	/****************************************************/
	/********************** View Handlers ***************/
	/****************************************************/

	@Handler
	protected Mav viewHBaseServers() {
		initializeGlobalParameters();
		mav.setViewName(PATH_JSP_HBASE + "hbaseServers.jsp");
		hbaseClient.getHBaseAdmin();
		HMasterInterface master = null;
		try {
			master = hbaseClient.getHBaseAdmin().getMaster();
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
		initializeGlobalParameters();
		mav.setViewName(PATH_JSP_HBASE + "hbaseTableSettings.jsp");
		String tableName = RequestTool.get(request, PARAM_tableName);
		HTableDescriptor table = null;
		try {
			table = hbaseClient.getHBaseAdmin().getTableDescriptor(
					tableName.getBytes());
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
		initializeGlobalParameters();
		initializeHBaseParameters();
		mav = new Mav(PATH_JSP_HBASE + "hbaseTableRegions.jsp");
		tableName = RequestTool.get(request, PARAM_tableName);
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
		initializeGlobalParameters();
		String tableName = RequestTool.get(request, PARAM_tableName);
		HBaseAdmin admin = hbaseClient.getHBaseAdmin();
		HTableDescriptor table = null;
		try {
			table = admin.getTableDescriptor(tableName.getBytes());

			if (table != null) {
				try {
					admin.disableTable(tableName);
					logger.warn("table disabled");
					Long maxFileSizeMb = RequestTool.getLong(request,
							PARAM_maxFileSizeMb, null);
					if (maxFileSizeMb != null) {
						table.setMaxFileSize(maxFileSizeMb * 1024 * 1024);
					}
					Long memstoreFlushSizeMb = RequestTool.getLong(request,
							PARAM_memstoreFlushSizeMb, null);
					if (memstoreFlushSizeMb != null) {
						table.setMemStoreFlushSize(memstoreFlushSizeMb * 1024 * 1024);
					}
					admin.modifyTable(StringByteTool.getUtf8Bytes(tableName),
							table);
				} catch (Exception e) {
					logger.warn(ExceptionTool.getStackTraceAsString(e));
				} finally {
					admin.enableTable(tableName);
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
		initializeGlobalParameters();
		String tableName = RequestTool.get(request, PARAM_tableName);
		HBaseAdmin admin = hbaseClient.getHBaseAdmin();
		HTableDescriptor table;
		try {
			table = admin.getTableDescriptor(tableName.getBytes());

			String columnName = RequestTool.get(request, PARAM_columnName);
			HColumnDescriptor column = table.getFamily(columnName.getBytes());
			try {
				// validate all settings before disabling table
				for (String colParam : IterableTool
						.nullSafe(DRHTableSettings.COLUMN_SETTINGS)) {
					String value = RequestTool.get(request, colParam);
					DRHTableSettings.validateColumnFamilySetting(colParam,
							value);
				}
				admin.disableTable(tableName);
				logger.warn("table disabled");
				for (String colParam : IterableTool
						.nullSafe(DRHTableSettings.COLUMN_SETTINGS)) {
					String value = RequestTool.get(request, colParam);
					column.setValue(colParam, value.trim());
				}
				admin.modifyColumn(tableName, column);
			} catch (Exception e) {
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			} finally {
				admin.enableTable(tableName);
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
		initializeGlobalParameters();
		initializeHBaseParameters();
		int counter = 0;
		for (DRHRegionInfo region : regionList.getRegions()) {
			if (!region.isOnCorrectServer()) {
				++counter;
				PhaseTimer timer = new PhaseTimer("move " + counter + " of "
						+ tableName);
				String encodedRegionNameString = region.getRegion()
						.getEncodedName();
				String destinationServer = region.getConsistentHashServerName()
						.getHostname();
				try {
					admin.move(Bytes.toBytes(encodedRegionNameString),
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
		initializeGlobalParameters();
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
		mav = new MessageMav("HBase moved  several table regions to server "
				+ "");

		return mav;
	}

	/****************************************************/
	/********************** Compact Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav compactHBaseTableRegions() {
		initializeGlobalParameters();
		initializeHBaseParameters();
		for (int i = 0; i < numRegions; ++i) {
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("compact " + i + "/" + numRegions
					+ " on " + tableName);
			DRHRegionInfo region = regionList
					.getRegionByEncodedName(encodedRegionNameString);
			if (region == null) {
				logger.warn(timer.add("couldn't find " + tableName + " region "
						+ encodedRegionNameString));
				continue;
			}
			try {
				admin.compact(region.getRegion().getRegionName());
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
		initializeGlobalParameters();
		initializeHBaseParameters();

		try {
			admin.compact(tableName);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		logger.warn("Submitted compact request for entire table " + tableName);
		mav = new MessageMav("Submitted compact request for entire table "
				+ tableName);

		// initializeMav();
		return mav;
	}

	@Handler
	protected Mav majorCompactHBaseTableRegions() {
		initializeGlobalParameters();
		initializeHBaseParameters();
		initializeMav();

		for (int i = 0; i < numRegions; ++i) {
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("majorCompact " + i + "/"
					+ numRegions + " on " + tableName);
			DRHRegionInfo region = regionList
					.getRegionByEncodedName(encodedRegionNameString);
			if (region == null) {
				logger.warn(timer.add("couldn't find " + tableName + " region "
						+ encodedRegionNameString));
				continue;
			}
			try {
				admin.majorCompact(region.getRegion().getRegionName());
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
		initializeGlobalParameters();
		initializeHBaseParameters();

		try {
			admin.majorCompact(tableName);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		logger.warn("Submitted majorCompact request for entire table "
				+ tableName);

		// initializeMav();
		mav = new MessageMav("Submitted majorCompact request for entire table "
				+ tableName);

		return mav;
	}

	/****************************************************/
	/********************** Flush Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav flushHBaseTableRegions() {
		initializeGlobalParameters();
		initializeHBaseParameters();

		for (int i = 0; i < numRegions; ++i) {
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("flush " + i + "/" + numRegions
					+ " on " + tableName);
			DRHRegionInfo region = regionList
					.getRegionByEncodedName(encodedRegionNameString);
			if (region == null) {
				logger.warn(timer.add("couldn't find " + tableName + " region "
						+ encodedRegionNameString));
				continue;
			}
			try {
				admin.flush(region.getRegion().getRegionName());
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
		initializeGlobalParameters();
		initializeHBaseParameters();
		try {
			admin.flush(tableName);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		logger.warn("Submitted flush request for entire table " + tableName);

		for (int i = 0; i < numRegions; ++i) {
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("flush " + i + "/" + numRegions
					+ " on " + tableName);
			DRHRegionInfo region = regionList
					.getRegionByEncodedName(encodedRegionNameString);
			if (region == null) {
				logger.warn(timer.add("couldn't find " + tableName + " region "
						+ encodedRegionNameString));
				continue;
			}
			try {
				admin.flush(region.getRegion().getRegionName());
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			logger.warn(timer.add("Submitted flush request region "
					+ encodedRegionNameString));
		}

		// initializeMav();
		mav = new MessageMav("Flushed all table regions ");

		return mav;
	}

	/****************************************************/
	/********************** Merge Handlers ***************/
	/****************************************************/
	@Handler
	protected Mav mergeFollowingHBaseTableRegions() {
		initializeGlobalParameters();
		initializeHBaseParameters();

		Merge merge = new Merge(hbaseConfig);
		for (int i = 0; i < numRegions; ++i) {
			PhaseTimer timer = new PhaseTimer("merge " + i + "/" + numRegions
					+ " on " + tableName);
			DRHRegionInfo regionA = regionList
					.getRegionByEncodedName(encodedRegionNameStrings.get(i));
			if (regionA == null) {
				logger.warn(timer.add("couldn't find " + tableName + " region "
						+ encodedRegionNameStrings.get(i)));
				continue;
			}
			DRHRegionInfo regionB = regionList
					.getRegionAfter(encodedRegionNameStrings.get(i));
			try {
				admin.flush(regionA.getRegion().getRegionName());
				admin.flush(regionB.getRegion().getRegionName());
				merge.run(new String[] { tableName,
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

}

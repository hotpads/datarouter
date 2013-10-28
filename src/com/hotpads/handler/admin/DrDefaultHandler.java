package com.hotpads.handler.admin;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.ipc.HMasterInterface;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHTableSettings;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.memory.MemoryClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.handler.util.node.NodeWrapper;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class DrDefaultHandler extends BaseHandler{
	private DataRouter router;
	@Inject private DataRouterContext dataRouterContext;
	private Mav mav;
	private String routerName = null;
	private String clientName = null;
	private Client client = null;
	private MemoryClient memoryClient = null;
	private HibernateClientImp hibernateClient = null;
	private HBaseClientImp hbaseClient = null;
	private Configuration hbaseConfig = null;
	private String nodeName = null;
	private Node<?,?> node = null;

	@Override
	@Handler
	protected Mav handleDefault(){
		return new Mav("/jsp/admin/index.jsp");
	}

	@Handler
	protected Mav listRouters(){
		initializeParameters();
		mav = mav.setViewName("/jsp/admin/datarouter/dataRouterMenu.jsp");
		List<DataRouter> routers = dataRouterContext.getRouters();
		initClients(routers);
		mav.put("serverName", dataRouterContext.getServerName());
		mav.put("administratorEmail", dataRouterContext.getAdministratorEmail());
		mav.put("routers", routers);
		return mav;
	}

	@Handler
	protected Mav inspectRouter(){
		initializeParameters();

		mav = mav.setViewName("/jsp/admin/datarouter/routerSummary.jsp");
		List<NodeWrapper> nodeWrappers = NodeWrapper.getNodeWrappers(router);
		mav.put("nodeWrappers", nodeWrappers);
		return mav;

	}

	@Handler
	protected Mav inspectClient(){
		initializeParameters();
		List<PhysicalNode<?,?>> nodes = dataRouterContext.getNodes().getPhysicalNodesForClient(clientName);
		mav.put("nodes", nodes);

		if(client instanceof MemoryClient){
			mav.setViewName("/jsp/admin/datarouter/memory/memoryClientSummary.jsp");
			mav.put("nodes", memoryClient.getNodes());
			return mav;

		}else if(client instanceof HibernateClientImp){
			mav.setViewName("/jsp/admin/datarouter/hibernate/hibernateClientSummary.jsp");
			mav.put("address", "TODO");
			mav.put("hibernateClientStats", hibernateClient.getStats());
			String[] tokens = hibernateClient.getSessionFactory().getStatistics().toString().split(",");
			List<String[]> sessionFactoryStats = ListTool.create();
			for(String token : tokens){
				sessionFactoryStats.add(token.split("="));
			}
			mav.put("sessionFactoryStats", sessionFactoryStats);
			return mav;

		}else if(client instanceof HBaseClientImp){
			mav.setViewName("/jsp/admin/datarouter/hbase/hbaseClientSummary.jsp");
			mav.put("address", hbaseConfig.get(HConstants.ZOOKEEPER_QUORUM));
			List<HTableDescriptor> tables = null;
			try{
				tables = ListTool.create(hbaseClient.getHBaseAdmin().listTables());
			}catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String,Map<String,String>> tableSummaryByName = MapTool.createTreeMap();
			@SuppressWarnings("unchecked") List<String> tableNamesForClient = router.getContext().getNodes()
					.getTableNamesForRouterAndClient(routerName, clientName);
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
		return new MessageMav("Client not found");

	}

	// @Handler
	// protected Mav countHBaseTableCells(){
	// String tableName = RequestTool.get(request, PARAM_tableName);
	// String timestamp = DateTool.getYYYYMMDDHHMMSS(new Date());
	// String jobName = "CellCounter_" + tableName + "_" + timestamp;
	// String outputPath = "cellcounter/"+tableName+"_"+timestamp+".txt";
	// String reportLink = new HBaseCellCounter(jobName, tableName, outputPath).call();
	// return new MessageMav("<a href=\""+reportLink+"\">link to output directory</a>");
	// return new MessageMav("Need to move HBaseCellCounter" );
	// }
	//
	// @Handler Mav copyHBaseTable(){
	// HBaseNode<?,?,?> hBaseNode = (HBaseNode<?,?,?>)node;
	// String timestamp = DateTool.getYYYYMMDDHHMMSS(new Date());
	// String jobName = "CopyHBaseTable_" + hBaseNode.getTableName() + "_" + timestamp;
	// String destinationTableName = StringTool.verifyNotEmpty(RequestTool.get(request, PARAM_destinationTableName));
	// PhaseTimer timer = new PhaseTimer("copying "+hBaseNode.getTableName());
	// new HBaseTableCopier(jobName, hBaseNode, destinationTableName).call();
	// timer.add("done");
	// return new MessageMav(timer.toString());
	// return new MessageMav("Need to move HBaseTableCopier" );
	//
	// }
	//
	// @Handler
	// protected Mav exportNodeToHFile(){
	// HBaseNode<?,?,?> hBaseNode = (HBaseNode<?,?,?>)node;
	// String timestamp = DateTool.getYYYYMMDDHHMMSS(new Date());
	// String jobName = "ExportNodeToHFile_" + hBaseNode.getTableName() + "_" + timestamp;
	// PhaseTimer timer = new PhaseTimer("copying "+hBaseNode.getTableName());
	// Job job = new HBaseHFileExporter(jobName, hBaseNode, "copytable/" + hBaseNode.getTableName() + "_"
	// + timestamp).call();
	// timer.add("done");
	// String jobTrackerHref = "http://localhost:50030/jobdetails.jsp?jobid="+job.getJobID()+"&refresh=10";
	// StringBuilder sb = new StringBuilder();
	// sb.append("export status:<br/><a href=\""+jobTrackerHref+"\">"+jobTrackerHref+"</a>");
	// sb.append("<br/><br/>complete bulk load with:<br/>");
	// sb.append("bin/hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles <hdfs://storefileoutput> <tablename>");
	// return new MessageMav(sb.toString());
	//
	// }
	@Handler
	protected Mav viewHBaseServers(){
		initializeParameters();
		mav.setViewName("/jsp/admin/datarouter/hbase/hbaseServers.jsp");
		hbaseClient.getHBaseAdmin();
		HMasterInterface master = null;
		try{
			master = hbaseClient.getHBaseAdmin().getMaster();
		}catch(MasterNotRunningException e){
			e.printStackTrace();
		}catch(ZooKeeperConnectionException e){
			e.printStackTrace();
		}
		if(master != null){
			ClusterStatus clusterStatus = master.getClusterStatus();
			mav.put("clusterStatus", clusterStatus);
			Collection<ServerName> serverNames = clusterStatus.getServers();
			List<DRHServerInfo> servers = ListTool.create();
			for(ServerName serverName : IterableTool.nullSafe(serverNames)){
				HServerLoad hServerLoad = clusterStatus.getLoad(serverName);
				servers.add(new DRHServerInfo(serverName, hServerLoad));
			}
			mav.put("servers", servers);
		}
		return mav;
	}

//	@Handler
//	protected Mav viewHBaseTableRegions(){
//		mav.setViewName("/jsp/admin/datarouter/hbase/hbaseTableRegions.jsp");
//		String tableName = RequestTool.get(request, "tableName");
//		String groupBy = RequestTool.get(request, "groupBy", null);
//		DRHServerList drhServerList = new DRHServerList(hbaseConfig);
//		DRHRegionList regionList = new DRHRegionList(hbaseClient, drhServerList, tableName, hbaseConfig, node,
//				HbaseRegionBalancerJob.getBalancerStrategyForTable(tableName), new HotPadsCompactionInfo());
//		mav.put("regionsByGroup", regionList.getRegionsGroupedBy(groupBy));
//		DRHServerList serverList = new DRHServerList(hbaseConfig);
//		mav.put("serverNames", serverList.getServerNames());
//		return mav;
//
//	}

	// @Handler
	// protected Mav moveRegionsToCorrectServer.equals(action)
	// || ACTION_moveHBaseTableRegions.equals(action)
	// || ACTION_compactHBaseTableRegions.equals(action)
	// || ACTION_compactAllHBaseTableRegions.equals(action)
	// || ACTION_majorCompactHBaseTableRegions.equals(action)
	// || ACTION_majorCompactAllHBaseTableRegions.equals(action)
	// || ACTION_flushHBaseTableRegions.equals(action)
	// || ACTION_flushAllHBaseTableRegions.equals(action)
	// || ACTION_mergeFollowingHBaseTableRegions.equals(action)){
	// // TODO don't tie to a specific table
	// String tableName = RequestTool.get(request, PARAM_tableName);
	// HBaseAdmin admin = hbaseClient.getHBaseAdmin();
	// List<String> encodedRegionNameStrings =
	// RequestTool.getCheckedBoxes(request, PARAM_PREFIX_encodedRegionName_);
	// int numRegions = CollectionTool.size(encodedRegionNameStrings);
	//
	// if(ACTION_moveRegionsToCorrectServer.equals(action)){
	// DRHServerList drhServerList = new DRHServerList(hbaseConfig);
	// DRHRegionList regionList =
	// new DRHRegionList(hbaseClient, drhServerList, tableName, hbaseConfig, node,
	// HbaseRegionBalancerJob.getBalancerStrategyForTable(tableName),
	// new HotPadsCompactionInfo());
	// int counter = 0;
	// for(DRHRegionInfo region : regionList.getRegions()){
	// if(!region.isOnCorrectServer()){
	// ++counter;
	// PhaseTimer timer = new PhaseTimer("move " + counter + " of " + tableName);
	// String encodedRegionNameString = region.getRegion().getEncodedName();
	// String destinationServer = region.getConsistentHashServerName().getHostname();
	// admin.move(Bytes.toBytes(encodedRegionNameString), Bytes.toBytes(destinationServer));
	// logger.warn(timer.add("HBase moved region " + encodedRegionNameString + " to server "
	// + destinationServer));
	// }
	// }
	// }
	// @Handler
	// protected Mav moveHBaseTableRegions(){
	// String destinationServer = RequestTool.get(request, PARAM_destinationServerName);
	// ServerName serverName = new ServerName(destinationServer);
	// DRHServerList serverList = new DRHServerList(hbaseConfig);
	// if(CollectionTool.doesNotContain(serverList.getServerNames(), serverName)){
	// throw new IllegalArgumentException(serverName + " not found");
	// }
	// for(int i = 0; i < numRegions; ++i){
	// String encodedRegionNameString = encodedRegionNameStrings.get(i);
	// PhaseTimer timer = new PhaseTimer("move " + i + "/" + numRegions + " of " + tableName);
	// admin.move(Bytes.toBytes(encodedRegionNameString), Bytes.toBytes(destinationServer));
	// logger.warn(timer.add("HBase moved region " + encodedRegionNameString + " to server "
	// + serverName));
	// }
	//
	// }
	//
	// @Handler
	// protected Mav compactHBaseTableRegions(){
	// DRHServerList drhServerList = new DRHServerList(hbaseConfig);
	// DRHRegionList regionList = new DRHRegionList(hbaseClient, drhServerList, tableName, hbaseConfig, node,
	// HbaseRegionBalancerJob.getBalancerStrategyForTable(tableName),
	// new HotPadsCompactionInfo());
	// for(int i = 0; i < numRegions; ++i){
	// String encodedRegionNameString = encodedRegionNameStrings.get(i);
	// PhaseTimer timer = new PhaseTimer("compact " + i + "/" + numRegions + " on " + tableName);
	// DRHRegionInfo region = regionList.getRegionByEncodedName(encodedRegionNameString);
	// if(region == null){
	// logger.warn(timer.add("couldn't find " + tableName + " region " + encodedRegionNameString));
	// continue;
	// }
	// admin.compact(region.getRegion().getRegionName());
	// logger.warn(timer.add("Submitted compact request region " + encodedRegionNameString));
	// }
	// } @Handler
	// protected Mav compactAllHBaseTableRegions(){
	// admin.compact(tableName);
	// logger.warn("Submitted compact request for entire table " + tableName);
	//
	// } @Handler
	// protected Mav majorCompactHBaseTableRegions(){
	// DRHServerList drhServerList = new DRHServerList(hbaseConfig);
	// DRHRegionList regionList = new DRHRegionList(hbaseClient, drhServerList, tableName, hbaseConfig, node,
	// HbaseRegionBalancerJob.getBalancerStrategyForTable(tableName), new HotPadsCompactionInfo());
	// for(int i = 0; i < numRegions; ++i){
	// String encodedRegionNameString = encodedRegionNameStrings.get(i);
	// PhaseTimer timer = new PhaseTimer("majorCompact " + i + "/" + numRegions + " on " + tableName);
	// DRHRegionInfo region = regionList.getRegionByEncodedName(encodedRegionNameString);
	// if(region == null){
	// logger.warn(timer.add("couldn't find " + tableName + " region " + encodedRegionNameString));
	// continue;
	// }
	// admin.majorCompact(region.getRegion().getRegionName());
	// logger.warn(timer.add("Submitted majorCompact request region " + encodedRegionNameString));
	// }
	// } @Handler
	// protected Mav majorCompactAllHBaseTableRegions(){
	// admin.majorCompact(tableName);
	// logger.warn("Submitted majorCompact request for entire table " + tableName);
	//
	// } @Handler
	// protected Mav flushHBaseTableRegions(){
	// DRHServerList drhServerList = new DRHServerList(hbaseConfig);
	// DRHRegionList regionList = new DRHRegionList(hbaseClient, drhServerList, tableName, hbaseConfig, node,
	// HbaseRegionBalancerJob.getBalancerStrategyForTable(tableName), new HotPadsCompactionInfo());
	// for(int i = 0; i < numRegions; ++i){
	// String encodedRegionNameString = encodedRegionNameStrings.get(i);
	// PhaseTimer timer = new PhaseTimer("flush " + i + "/" + numRegions + " on " + tableName);
	// DRHRegionInfo region = regionList.getRegionByEncodedName(encodedRegionNameString);
	// if(region == null){
	// logger.warn(timer.add("couldn't find " + tableName + " region " + encodedRegionNameString));
	// continue;
	// }
	// admin.flush(region.getRegion().getRegionName());
	// logger.warn(timer.add("Submitted flush request region " + encodedRegionNameString));
	// }
	// }
	//
	//
	// @Handler
	// protected Mav flushAllHBaseTableRegions(){
	// admin.flush(tableName);
	// logger.warn("Submitted flush request for entire table " + tableName);
	//
	// }
	//
	//
	//
	// @Handler
	// protected Mav mergeFollowingHBaseTableRegions(){
	// DRHServerList drhServerList = new DRHServerList(hbaseConfig);
	// DRHRegionList regionList = new DRHRegionList(hbaseClient, drhServerList, tableName, hbaseConfig, node,
	// HbaseRegionBalancerJob.getBalancerStrategyForTable(tableName),
	// new HotPadsCompactionInfo());
	// Merge merge = new Merge(hbaseConfig);
	// for(int i = 0; i < numRegions; ++i){
	// PhaseTimer timer = new PhaseTimer("merge " + i + "/" + numRegions + " on " + tableName);
	// DRHRegionInfo regionA = regionList.getRegionByEncodedName(encodedRegionNameStrings.get(i));
	// if(regionA == null){
	// logger.warn(timer.add("couldn't find " + tableName + " region "
	// + encodedRegionNameStrings.get(i)));
	// continue;
	// }
	// DRHRegionInfo regionB = regionList.getRegionAfter(encodedRegionNameStrings.get(i));
	// admin.flush(regionA.getRegion().getRegionName());
	// admin.flush(regionB.getRegion().getRegionName());
	// merge.run(new String[]{tableName, regionA.getRegion().getRegionNameAsString(),
	// regionB.getRegion().getRegionNameAsString()});
	// logger.warn(timer.add("merged " + regionA.getRegion().getRegionNameAsString() + " and "
	// + regionB.getRegion().getRegionNameAsString()));
	// }
	// }
	// mav.setViewName("redirect:/admin/dataRouter/menu.htm");
	// mav.put(RequestTool.SUBMIT_ACTION, ACTION_viewHBaseTableRegions);
	// mav.put(PARAM_routerName, routerName);
	// mav.put(PARAM_clientName, clientName);
	// mav.put(PARAM_tableName, tableName);
	// return mav;
	//
	// }

	@Handler
	protected Mav viewHBaseTableSettings(){
		initializeParameters();
		mav.setViewName("/jsp/admin/datarouter/hbase/hbaseTableSettings.jsp");
		String tableName = RequestTool.get(request, PARAM_tableName);
		HTableDescriptor table = null;
		try{
			table = hbaseClient.getHBaseAdmin().getTableDescriptor(tableName.getBytes());
		}catch(TableNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
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
	protected Mav updateHBaseTableAttribute(){
		initializeParameters();
		String tableName = RequestTool.get(request, PARAM_tableName);
		HBaseAdmin admin = hbaseClient.getHBaseAdmin();
		HTableDescriptor table = null;
		try{
			table = admin.getTableDescriptor(tableName.getBytes());

			if(table != null){
				try{
					admin.disableTable(tableName);
					logger.warn("table disabled");
					Long maxFileSizeMb = RequestTool.getLong(request, PARAM_maxFileSizeMb, null);
					if(maxFileSizeMb != null){
						table.setMaxFileSize(maxFileSizeMb * 1024 * 1024);
					}
					Long memstoreFlushSizeMb = RequestTool.getLong(request, PARAM_memstoreFlushSizeMb, null);
					if(memstoreFlushSizeMb != null){
						table.setMemStoreFlushSize(memstoreFlushSizeMb * 1024 * 1024);
					}
					admin.modifyTable(StringByteTool.getUtf8Bytes(tableName), table);
				}catch(Exception e){
					logger.warn(ExceptionTool.getStackTraceAsString(e));
				}finally{
					admin.enableTable(tableName);
				}
				logger.warn("table enabled");
				//TODO Update when the handler is created on datarouter
				mav.setViewName("redirect:/admin/dataRouter/menu.htm");
				mav.put(RequestTool.SUBMIT_ACTION, ACTION_viewHBaseTableRegions);
				mav.put(PARAM_routerName, RequestTool.get(request, PARAM_routerName));
				mav.put(PARAM_clientName, RequestTool.get(request, PARAM_clientName));
				mav.put(PARAM_tableName, RequestTool.get(request, PARAM_tableName));
			}
		}catch(TableNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e1){
			e1.printStackTrace();
		}
		return mav;

	}

	@Handler
	protected Mav updateHBaseColumnAttribute(){
		initializeParameters();
		String tableName = RequestTool.get(request, PARAM_tableName);
		HBaseAdmin admin = hbaseClient.getHBaseAdmin();
		HTableDescriptor table;
		try{
			table = admin.getTableDescriptor(tableName.getBytes());

			String columnName = RequestTool.get(request, PARAM_columnName);
			HColumnDescriptor column = table.getFamily(columnName.getBytes());
			try{
				// validate all settings before disabling table
				for(String colParam : IterableTool.nullSafe(DRHTableSettings.COLUMN_SETTINGS)){
					String value = RequestTool.get(request, colParam);
					DRHTableSettings.validateColumnFamilySetting(colParam, value);
				}
				admin.disableTable(tableName);
				logger.warn("table disabled");
				for(String colParam : IterableTool.nullSafe(DRHTableSettings.COLUMN_SETTINGS)){
					String value = RequestTool.get(request, colParam);
					column.setValue(colParam, value.trim());
				}
				admin.modifyColumn(tableName, column);
			}catch(Exception e){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}finally{
				admin.enableTable(tableName);
			}
		}catch(TableNotFoundException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch(IOException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		logger.warn("table enabled");
		//TODO Update when the handler is created on datarouter
		mav.setViewName("redirect:/admin/dataRouter/menu.htm");
		mav.put(RequestTool.SUBMIT_ACTION, ACTION_viewHBaseTableRegions);
		mav.put(PARAM_routerName, RequestTool.get(request, PARAM_routerName));
		mav.put(PARAM_clientName, RequestTool.get(request, PARAM_clientName));
		mav.put(PARAM_tableName, RequestTool.get(request, PARAM_tableName));
		return mav;

	}

	/************************* Utils methods ***********************************/
	private void initializeParameters(){
		String action = RequestTool.getSubmitAction(request, ACTION_listRouters);
		mav = new Mav();
		/********************** common params *************************************/

		if(NEEDS_ROUTER.contains(action)){
			routerName = RequestTool.get(request, PARAM_routerName);
			router = dataRouterContext.getRouter(routerName);
			mav.put("router", router);
		}
		if(NEEDS_CLIENT.contains(action)){
			clientName = RequestTool.get(request, PARAM_clientName);
			client = getClient(request);
			mav.put("client", client);
			if(client instanceof MemoryClient){
				memoryClient = (MemoryClient)client;
			}else if(client instanceof HibernateClientImp){
				hibernateClient = (HibernateClientImp)client;
			}else if(client instanceof HBaseClientImp){
				hbaseClient = (HBaseClientImp)client;
				hbaseConfig = hbaseClient.getHBaseConfiguration();
			}
		}
		if(NEEDS_NODE.contains(action)){
			nodeName = RequestTool.get(request, PARAM_nodeName, null);
			if(StringTool.notEmpty(nodeName)){
				node = dataRouterContext.getRouter(routerName).getContext().getNodes().getNode(nodeName);
			}else{
				String tableName = RequestTool.get(request, PARAM_tableName, null);
				if(tableName != null){
					node = dataRouterContext.getRouter(routerName).getContext().getNodes()
							.getPhyiscalNodeForClientAndTable(clientName, tableName);
				}
			}
		}
	}

	public static void initClients(Collection<DataRouter> routers){
		List<String> allClientNames = ListTool.create();
		for(final DataRouter router : IterableTool.nullSafe(routers)){
			allClientNames.addAll(CollectionTool.nullSafe(router.getClientNames()));
		}
		ExecutorService exec = Executors.newFixedThreadPool(CollectionTool.size(allClientNames));
		for(final DataRouter router : IterableTool.nullSafe(routers)){
			for(final String clientName : IterableTool.nullSafe(router.getClientNames())){
				exec.submit(new Callable<Void>(){
					public Void call(){
						router.getClient(clientName);
						return null;
					}
				});
			}
		}
		exec.shutdown();
	}

	protected Client getClient(HttpServletRequest request){
		String routerName = RequestTool.get(request, PARAM_routerName);
		String clientName = RequestTool.get(request, PARAM_clientName);
		Client baseClient = dataRouterContext.getRouter(routerName).getClient(clientName);
		return baseClient;
	}

	protected Map<String,String> parseFamilyAttributeMap(Map<ImmutableBytesWritable,ImmutableBytesWritable> ins){
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

	/************************************** Constants **********************************/
	Logger logger = Logger.getLogger(getClass());

	public static final String ACTION_listRouters = "listRouters",
			ACTION_inspectRouter = "inspectRouter",
			ACTION_inspectClient = "inspectClient",

			// hbase
			ACTION_countHBaseTableCells = "countHBaseTableCells", ACTION_copyHBaseTable = "copyHBaseTable",
			ACTION_exportNodeToHFile = "exportNodeToHFile", ACTION_viewHBaseServers = "viewHBaseServers",
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
			ACTION_updateHBaseColumnAttribute = "updateHBaseColumnAttribute",

			PARAM_routerName = "routerName", PARAM_clientName = "clientName", PARAM_nodeName = "nodeName",
			PARAM_tableName = "tableName", PARAM_columnName = "columnName",
			PARAM_destinationTableName = "destinationTableName",
			PARAM_PREFIX_encodedRegionName_ = "encodedRegionName_",
			PARAM_destinationServerName = "destinationServerName", PARAM_maxFileSizeMb = "maxFileSizeMb",
			PARAM_memstoreFlushSizeMb = "memstoreFlushSizeMb";

	public static final List<String> NEEDS_CLIENT = ListTool.create();
	static{
		NEEDS_CLIENT.add(ACTION_inspectClient);
		NEEDS_CLIENT.add(ACTION_viewHBaseServers);
		NEEDS_CLIENT.add(ACTION_viewHBaseTableRegions);
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
	}

	public static final List<String> NEEDS_ROUTER = ListTool.create();
	static{
		NEEDS_ROUTER.addAll(NEEDS_CLIENT);
		NEEDS_ROUTER.add(ACTION_inspectRouter);
		NEEDS_ROUTER.add(ACTION_copyHBaseTable);
		NEEDS_ROUTER.add(ACTION_exportNodeToHFile);
	}

	public static final List<String> NEEDS_NODE = ListTool.create();
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

	public static final String
	// also hard-coded in hbaseTableSettings.jsp
			HBASE_TABLE_PARAM_MAX_FILESIZE = "MAX_FILESIZE",
			HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE = "MEMSTORE_FLUSHSIZE";

	public static final List<String> HBASE_TABLE_PARAMS = ListTool.create();
	static{
		HBASE_TABLE_PARAMS.add(HBASE_TABLE_PARAM_MAX_FILESIZE);
		HBASE_TABLE_PARAMS.add(HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE);
	}
}

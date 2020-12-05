/**
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
package io.datarouter.client.hbase.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.HBaseClientType;
import io.datarouter.client.hbase.balancer.HBaseBalancerFactory;
import io.datarouter.client.hbase.cluster.DrRegionInfo;
import io.datarouter.client.hbase.cluster.DrRegionListFactory;
import io.datarouter.client.hbase.cluster.DrRegionListFactory.DrRegionList;
import io.datarouter.client.hbase.cluster.DrServerInfo;
import io.datarouter.client.hbase.cluster.DrServerList;
import io.datarouter.client.hbase.cluster.DrTableSettings;
import io.datarouter.client.hbase.config.DatarouterHBaseFiles;
import io.datarouter.client.hbase.util.HBaseClientTool;
import io.datarouter.client.hbase.util.ServerNameTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory.DatarouterWebRequestParams;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalInteger;
import io.datarouter.web.handler.types.optional.OptionalLong;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.util.http.RequestTool;

public class HBaseHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(HBaseHandler.class);

	private static final String PARAM_PREFIX_encodedRegionName_ = "encodedRegionName_";

	// also hard-coded in hbaseTableSettings.jsp
	private static final String HBASE_TABLE_PARAM_MAX_FILESIZE = "MAX_FILESIZE";
	private static final String HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE = "MEMSTORE_FLUSHSIZE";

	@Inject
	private HBaseBalancerFactory balancerFactory;
	@Inject
	private DatarouterWebRequestParamsFactory datarouterWebRequestParamsFactory;
	@Inject
	private DatarouterHBaseFiles files;
	@Inject
	private DrRegionListFactory drRegionListFactory;
	@Inject
	private HBaseClientManager hBaseClientManager;

	private DatarouterWebRequestParams<?> datarouterWebRequestParams;
	private int numRegions;
	private List<String> encodedRegionNameStrings;
	private Mav mav;
	private Supplier<DrServerList> drServerList;
	private Supplier<DrRegionList> regionList;

	/*---------------------------- useful methods ----------------------------*/

	private void initialize(){
		mav = new Mav();
		encodedRegionNameStrings = RequestTool.getCheckedBoxes(request, PARAM_PREFIX_encodedRegionName_);
		numRegions = encodedRegionNameStrings.size();
		datarouterWebRequestParams = datarouterWebRequestParamsFactory.new DatarouterWebRequestParams<>(params,
				getClientType());
		mav.put("clientType", datarouterWebRequestParams.getClientType().getName());
		drServerList = SingletonSupplier.of(() -> new DrServerList(hBaseClientManager.getAdmin(
				datarouterWebRequestParams.getClientId())));
		regionList = SingletonSupplier.of(() -> drRegionListFactory.make(
				datarouterWebRequestParams.getClientId(),
				drServerList.get(),
				datarouterWebRequestParams.getTableName(),
				datarouterWebRequestParams.getPhysicalNode(),
				balancerFactory.getBalancerForTable(datarouterWebRequestParams.getClientId(),
				datarouterWebRequestParams.getTableName())));
	}

	/*---------------------------- view handlers ----------------------------*/

	protected Class<? extends ClientType<?,?>> getClientType(){
		return HBaseClientType.class;
	}

	@Handler
	public Mav viewHBaseServers() throws IOException{
		initialize();
		mav.setViewName(files.jsp.admin.datarouter.hbase.hbaseServersJsp);
		ClusterStatus clusterStatus = hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId())
				.getClusterStatus();
		mav.put("clusterStatus", clusterStatus);
		Collection<ServerName> serverNames = new TreeSet<>(clusterStatus.getServers());
		List<DrServerInfo> servers = new ArrayList<>();
		for(ServerName serverName : serverNames){
			ServerLoad serverLoad = clusterStatus.getLoad(serverName);
			servers.add(new DrServerInfo(serverName, serverLoad));
		}
		mav.put("servers", servers);
		return mav;
	}

	@Handler
	public Mav viewHBaseTableSettings() throws TableNotFoundException, IOException{
		initialize();
		mav.setViewName(files.jsp.admin.datarouter.hbase.hbaseTableSettingsJsp);
		HTableDescriptor table;
		table = hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).getTableDescriptor(TableName
				.valueOf(datarouterWebRequestParams.getTableName()));
		// table level settings
		Map<String,String> tableParamByName = new TreeMap<>();
		tableParamByName.put(HBASE_TABLE_PARAM_MAX_FILESIZE, table.getMaxFileSize() / 1024 / 1024 + "");
		tableParamByName.put(HBASE_TABLE_PARAM_MEMSTORE_FLUSHSIZE, table.getMemStoreFlushSize() / 1024 / 1024 + "");
		mav.put("tableParamByName", tableParamByName);

		// column family level settings
		List<HColumnDescriptor> columnFamilies = Scanner.of(table.getColumnFamilies()).list();
		Map<String,Map<String,String>> columnSummaryByName = new TreeMap<>();
		for(HColumnDescriptor column : columnFamilies){
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
	public Mav viewHBaseTableRegions(OptionalString groupBy){
		initialize();
		mav = new Mav(files.jsp.admin.datarouter.hbase.hbaseTableRegionsJsp);
		mav.put("regionsByGroup", regionList.get().getRegionsGroupedBy(groupBy.orElse(DrRegionList.GROUP_BY_ALL)));
		mav.put("serverNames", drServerList.get().getServerNames());
		return mav;
	}

	/*---------------------------- update handlers --------------------------*/

	@Handler
	public Mav updateHBaseTableAttribute(OptionalLong maxFileSizeMb, OptionalLong memstoreFlushSizeMb)
	throws IOException{
		initialize();
		Admin admin = hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId());
		HTableDescriptor table;
		table = admin.getTableDescriptor(TableName.valueOf(datarouterWebRequestParams.getTableName().getBytes()));
		try{
			admin.disableTable(TableName.valueOf(datarouterWebRequestParams.getTableName()));
			logger.warn("table disabled");
			maxFileSizeMb
					.map(mb -> mb * 1024 * 1024)
					.ifPresent(table::setMaxFileSize);
			memstoreFlushSizeMb
					.map(mb -> mb * 1024 * 1024)
					.ifPresent(table::setMemStoreFlushSize);
			admin.modifyTable(TableName.valueOf(StringByteTool.getUtf8Bytes(datarouterWebRequestParams.getTableName())),
					table);
		}finally{
			admin.enableTable(TableName.valueOf(datarouterWebRequestParams.getTableName()));
		}
		logger.warn("table enabled");
		mav = new MessageMav("HBase table attributes updated");
		return mav;

	}

	@Handler
	public Mav updateHBaseColumnAttribute(String columnName) throws TableNotFoundException, IOException{
		initialize();
		Admin admin = hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId());
		HTableDescriptor table;
		table = admin.getTableDescriptor(TableName.valueOf(datarouterWebRequestParams.getTableName()));
		HColumnDescriptor column = table.getFamily(columnName.getBytes());
		try{
			// validate all settings before disabling table
			for(String colParam : DrTableSettings.COLUMN_SETTINGS){
				String value = params.required(colParam);
				DrTableSettings.validateColumnFamilySetting(colParam, value);
			}
			admin.disableTable(TableName.valueOf(datarouterWebRequestParams.getTableName()));
			logger.warn("table disabled");
			for(String colParam : DrTableSettings.COLUMN_SETTINGS){
				String value = params.required(colParam);
				column.setValue(colParam, value.trim());
			}
			admin.modifyColumn(TableName.valueOf(datarouterWebRequestParams.getTableName()), column);
		}catch(Exception e){
			logger.warn("", e);
		}finally{
			admin.enableTable(TableName.valueOf(datarouterWebRequestParams.getTableName()));
		}
		// initializeMav();
		mav = new MessageMav("HBase column attributes updated");
		return mav;

	}

	/*---------------------------- move handlers ----------------------------*/

	@Handler
	public Mav moveRegionsToCorrectServer(OptionalInteger pauseBetweenRegionsMs) throws IOException{
		initialize();
		int counter = 0;
		for(DrRegionInfo<?> region : regionList.get().getRegions()){
			if(!region.isOnCorrectServer()){
				++counter;
				PhaseTimer timer = new PhaseTimer("move " + counter + " of " + datarouterWebRequestParams
						.getTableName());
				String encodedRegionNameString = region.getRegion().getEncodedName();
				String destinationServer = region.getConsistentHashServerName().getServerName();
				hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).move(Bytes.toBytes(
						encodedRegionNameString), Bytes.toBytes(destinationServer));
				logger.warn(timer.add("HBase moved region " + encodedRegionNameString + " to server "
						+ destinationServer).toString());
			}
			ThreadTool.sleepUnchecked(pauseBetweenRegionsMs.orElse(500));
		}

		// mav.put("message-update", "HBase regions moved to correct server");
		return new MessageMav("HBase regions moved to correct server");
	}

	@Handler
	public Mav moveHBaseTableRegions(String tableName, String destinationServerName) throws IOException{
		initialize();
		ServerName serverName = ServerNameTool.create(destinationServerName);
		if(!drServerList.get().getServerNames().contains(serverName)){
			throw new IllegalArgumentException(serverName + " not found");
		}
		for(int i = 0; i < numRegions; ++i){
			String encodedRegionNameString = encodedRegionNameStrings.get(i);
			PhaseTimer timer = new PhaseTimer("move " + i + "/" + numRegions + " of " + tableName);
			hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).move(Bytes.toBytes(
					encodedRegionNameString), Bytes.toBytes(destinationServerName));
			logger.warn(timer.add("HBase moved region " + encodedRegionNameString + " to server " + serverName)
					.toString());
		}
		return new MessageMav("moved regions:" + encodedRegionNameStrings);
	}


	/*---------------------------- compact handlers -------------------------*/

	@Handler
	public Mav compactHBaseTableRegions() throws IOException{
		initialize();
		for(String encodedRegionNameString : encodedRegionNameStrings){
			DrRegionInfo<?> region = regionList.get().getRegionByEncodedName(encodedRegionNameString);
			hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).compactRegion(region.getRegion()
					.getRegionName());
		}
		return new MessageMav("compactions requested for regions:" + encodedRegionNameStrings);
	}

	@Handler
	public Mav compactAllHBaseTableRegions() throws IOException{
		initialize();
		hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).compact(TableName.valueOf(
				datarouterWebRequestParams.getTableName()));
		return new MessageMav("Submitted compact request for entire table " + datarouterWebRequestParams
				.getTableName());
	}

	@Handler
	public Mav majorCompactHBaseTableRegions() throws IOException{
		initialize();
		for(String encodedRegionNameString : encodedRegionNameStrings){
			DrRegionInfo<?> region = regionList.get().getRegionByEncodedName(encodedRegionNameString);
			hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).majorCompactRegion(region.getRegion()
					.getRegionName());
		}
		return new MessageMav("submitted major compaction requests for regions: " + encodedRegionNameStrings);
	}

	@Handler
	public Mav majorCompactAllHBaseTableRegions() throws IOException{
		initialize();
		hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).majorCompact(TableName.valueOf(
				datarouterWebRequestParams.getTableName()));
		return new MessageMav("Submitted majorCompact request for entire table " + datarouterWebRequestParams
				.getTableName());
	}

	/*--------------------------- flush Handlers ----------------------------*/

	@Handler
	public Mav flushHBaseTableRegions() throws IOException{
		initialize();
		for(String encodedRegionNameString : encodedRegionNameStrings){
			DrRegionInfo<?> region = regionList.get().getRegionByEncodedName(encodedRegionNameString);
			hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).flushRegion(region.getRegion()
					.getRegionName());
		}
		return new MessageMav("flushes requested for regions: " + encodedRegionNameStrings);
	}

	@Handler
	public Mav flushAllHBaseTableRegions() throws IOException{
		initialize();
		hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).flush(TableName.valueOf(
				datarouterWebRequestParams.getTableName()));
		return new MessageMav("Flushed all table regions ");
	}

	/*---------------------------- split regions ----------------------------*/

	@Handler
	public Mav splitPartitions() throws Exception{
		initialize();
		TableName tableName = TableName.valueOf(datarouterWebRequestParams.getTableName());
		byte[][] splitPoints = HBaseClientTool.getSplitPoints(NodeTool.extractSinglePhysicalNode(
				datarouterWebRequestParams.getNode()));
		for(byte[] splitPoint : splitPoints){
			try{
				hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).split(tableName, splitPoint);
			}catch(IOException e){
				if(e.getMessage().equals("should not give a splitkey which equals to startkey!")){
					logger.warn("split at {} already exists", Bytes.toStringBinary(splitPoint), e);
				}else{
					throw e;
				}
			}
			// the split function is fast
			// give some time to hbase to do its internal stuff so its region map is updated
			try{
				Thread.sleep(5000);
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		List<HRegionInfo> infos = hBaseClientManager.getAdmin(datarouterWebRequestParams.getClientId()).getTableRegions(
				tableName);
		for(byte[] splitPoint : splitPoints){
			boolean containsRegion = false;
			for(HRegionInfo info : infos){
				if(Arrays.equals(info.getStartKey(), splitPoint)){
					containsRegion = true;
				}
			}
			if(!containsRegion){
				throw new Exception("Regions were not split properly splitPoint=" + Bytes.toStringBinary(splitPoint));
			}
		}
		return new MessageMav("Split regions by partitions");
	}

	/*---------------------------- merge handlers ---------------------------*/

	private static Map<String,String> parseFamilyAttributeMap(Map<ImmutableBytesWritable,ImmutableBytesWritable> ins){
		Map<String,String> outs = new TreeMap<>();
		for(Entry<ImmutableBytesWritable,ImmutableBytesWritable> entry : ins.entrySet()){
			outs.put(
					StringByteTool.fromUtf8Bytes(entry.getKey().get()),
					StringByteTool.fromUtf8Bytes(entry.getValue().get()));
		}
		if(!outs.containsKey(DrTableSettings.DATA_BLOCK_ENCODING)){
			outs.put(DrTableSettings.DATA_BLOCK_ENCODING, DrTableSettings.DEFAULT_DATA_BLOCK_ENCODING);
		}
		if(!outs.containsKey(DrTableSettings.ENCODE_ON_DISK)){
			outs.put(DrTableSettings.ENCODE_ON_DISK, DrTableSettings.DEFAULT_ENCODE_ON_DISK);
		}
		return outs;
	}

}

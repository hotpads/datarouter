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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.config.DatarouterHBaseFiles;
import io.datarouter.client.hbase.config.DatarouterHBasePaths;
import io.datarouter.pathnode.PathNode;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;

@Singleton
public class HBaseWebInspector implements DatarouterClientWebInspector{

	@Inject
	private ClientOptions clientOptions;
	@Inject
	private DatarouterHBaseFiles files;
	@Inject
	private DatarouterHBasePaths datarouterHBasePaths;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;
	@Inject
	private HBaseClientManager hBaseClientManager;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, ClientType.class);
		var clientId = clientParams.getClientId();
		if(clientId == null){
			return new MessageMav("Client not found");
		}

		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var clientPageHeader = buildClientPageHeader(clientName);
		var clientOptionsTable = buildClientOptionsTable(allClientOptions);

		Mav mav = new Mav();
		mav.setViewName(files.jsp.admin.datarouter.hbase.hbaseClientSummaryJsp);
		mav.put("clientPageHeader", clientPageHeader.render());
		mav.put("clientOptionsTable", clientOptionsTable.render());
		mav.put("address", hBaseClientManager.getConnection(clientId).getConfiguration().get(
				HConstants.ZOOKEEPER_QUORUM));
		List<HTableDescriptor> tables;
		try{
			tables = List.of(hBaseClientManager.getAdmin(clientId).listTables());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Map<String,Map<String,String>> tableSummaryByName = new TreeMap<>();
		Map<String,Map<String,Map<String,String>>> familySummaryByTableName = new TreeMap<>();
		List<String> tableNamesForClient = datarouterNodes.getTableNamesForClient(clientName);
		for(HTableDescriptor table : tables){
			String tableName = table.getNameAsString();
			if(!tableNamesForClient.contains(tableName)){
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
		}
		mav.put("tableSummaryByName", tableSummaryByName);
		mav.put("familySummaryByTableName", familySummaryByTableName);
		mav.put("clientType", clientParams.getClientType().getName());
		mav.put("hbaseHandlerPath", getHandlerPath().toSlashedString());
		return mav;
	}

	protected PathNode getHandlerPath(){
		return datarouterHBasePaths.datarouter.clients.hbase;
	}

	private static Map<String,Map<String,String>> parseTableAttributeMap(Collection<HColumnDescriptor> families){
		Map<String,Map<String,String>> familyAttributeByNameByFamilyName = new TreeMap<>();
		for(HColumnDescriptor family : families){
			Map<String,String> familyAttributeByName = new TreeMap<>();
			familyAttributeByNameByFamilyName.put(family.getNameAsString(), familyAttributeByName);
			for(Entry<ImmutableBytesWritable,ImmutableBytesWritable> e : family.getValues().entrySet()){
				String key = Bytes.toString(e.getKey().get());
				String value = Bytes.toString(e.getValue().get());
				familyAttributeByName.put(key, value);
			}
		}
		return familyAttributeByNameByFamilyName;
	}

}

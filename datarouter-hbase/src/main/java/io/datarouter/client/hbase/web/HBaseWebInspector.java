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

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.config.DatarouterHBaseFiles;
import io.datarouter.client.hbase.config.DatarouterHBasePaths;
import io.datarouter.httpclient.path.PathNode;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory.DatarouterWebRequestParams;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;

@Singleton
public class HBaseWebInspector implements DatarouterClientWebInspector{

	@Deprecated// PathNodes
	public static final String PATH_JSP_HBASE = "/jsp/admin/datarouter/hbase/";

	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterWebRequestParamsFactory datarouterWebRequestParamsFactory;
	@Inject
	private DatarouterHBaseFiles files;
	@Inject
	private HBaseClientManager hBaseClientManager;
	@Inject
	private DatarouterHBasePaths datarouterHBasePaths;

	@Override
	public Mav inspectClient(Params params){
		Mav mav = new Mav();
		DatarouterWebRequestParams<?> routerParams = datarouterWebRequestParamsFactory.new DatarouterWebRequestParams<>(
				params, ClientType.class);
		if(routerParams.getClientId() == null){
			return new MessageMav("Client not found");
		}

		mav.setViewName(files.jsp.admin.datarouter.hbase.hbaseClientSummaryJsp);
		mav.put("address", hBaseClientManager.getConnection(routerParams.getClientId()).getConfiguration().get(
				HConstants.ZOOKEEPER_QUORUM));
		List<HTableDescriptor> tables;
		try{
			tables = ListTool.create(hBaseClientManager.getAdmin(routerParams.getClientId()).listTables());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Map<String,Map<String,String>> tableSummaryByName = new TreeMap<>();
		Map<String,Map<String,Map<String,String>>> familySummaryByTableName = new TreeMap<>();
		List<String> tableNamesForClient = getTableNames(routerParams);
		for(HTableDescriptor table : IterableTool.nullSafe(tables)){
			String tableName = table.getNameAsString();
			if(!CollectionTool.nullSafe(tableNamesForClient).contains(tableName)){
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
		mav.put("clientType", routerParams.getClientType().getName());
		mav.put("hbaseHandlerPath", getHandlerPath().toSlashedString());
		return mav;
	}

	protected PathNode getHandlerPath(){
		return datarouterHBasePaths.datarouter.clients.hbase;
	}

	private static Map<String,Map<String,String>> parseTableAttributeMap(Collection<HColumnDescriptor> families){
		Map<String,Map<String,String>> familyAttributeByNameByFamilyName = new TreeMap<>();
		for(HColumnDescriptor family : IterableTool.nullSafe(families)){
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

	private List<String> getTableNames(DatarouterWebRequestParams<?> routerParams){
		return datarouterNodes.getTableNamesForClient(routerParams.getClientId().getName());
	}

}

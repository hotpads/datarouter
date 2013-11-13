package com.hotpads.handler.httpclient;

import java.util.List;

import javax.inject.Inject;

import net.sf.json.JSON;

import com.hotpads.datarouter.client.imp.http.node.HttpReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Config.ConfigFielder;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderHttpNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.JsonMav;
import com.hotpads.handler.mav.imp.MessageMav;

/*
 * http://localhost:8080/analytics/datarouter/httpNode?submitAction=get&routerName=search&nodeName=search.Listing&key={%22feedId%22:%22HomeRentals%22,%22feedListingId%22:%22AL010006L%22}
 */
public class DataRouterHttpClientHandler<
		PK extends PrimaryKey<PK>,//handles a request for one node at a time, so the generics work
		D extends Databean<PK,D>>
extends BaseHandler
implements MapStorageReaderHttpNode<PK,D>{
	
	private static final ConfigFielder CONFIG_FIELDER = new ConfigFielder();
	
	public static final String
		PARAM_routerName = "routerName",
		PARAM_nodeName = "nodeName";
	
	@Inject
	private DataRouterContext drContext;
	
	private String routerName;
	private String nodeName;
	private DataRouter router;
	private MapStorageReaderNode<PK,D> node;
	private DatabeanFieldInfo<PK,D,?> fieldInfo;
	private Config config;

	@Override
	@Handler
	protected Mav handleDefault(){
		return new MessageMav("hello");
	}
	
	private void preHandle(){
		routerName = params.required(PARAM_routerName);
		nodeName = params.required(PARAM_nodeName);
		router = drContext.getRouter(routerName);
		node = (MapStorageReaderNode<PK,D>)drContext.getNodes().getNode(nodeName);
		fieldInfo = node.getFieldInfo();
		String configJsonString = params.optional(HttpReaderNode.PARAM_config, null);
		if(configJsonString != null){
			config = JsonDatabeanTool.databeanFromJson(Config.class, CONFIG_FIELDER, configJsonString);
		}
	}
	
	@Override
	@Handler
	public JsonMav get(){
		preHandle();
		PK key = JsonDatabeanTool.primaryKeyFromJson(
				fieldInfo.getPrimaryKeyClass(),
				fieldInfo.getSampleFielder().getKeyFielder(),
				params.required(HttpReaderNode.METHOD_get_PARAM_key));
		D databean = node.get(key, config);
		JSON jsonDatabean = JsonDatabeanTool.databeanToJson(databean, fieldInfo.getSampleFielder());
		return new JsonMav(jsonDatabean);
	}

	@Override
	@Handler
	public JsonMav getMulti(){
		preHandle();
		List<PK> keys = JsonDatabeanTool.primaryKeysFromJson(
				fieldInfo.getPrimaryKeyClass(),
				fieldInfo.getSampleFielder().getKeyFielder(),
				params.required(HttpReaderNode.METHOD_getMulti_PARAM_keys));
		List<D> databeans = node.getMulti(keys, config);
		JSON jsonDatabeans = JsonDatabeanTool.databeansToJson(databeans, fieldInfo.getSampleFielder());
		return new JsonMav(jsonDatabeans);
	}

	@Override
	@Handler
	public JsonMav getAll(){
		preHandle();
		List<D> databeans = node.getAll(config);
		JSON jsonDatabeans = JsonDatabeanTool.databeansToJson(databeans, fieldInfo.getSampleFielder());
		return new JsonMav(jsonDatabeans);
	}

	@Override
	@Handler
	public JsonMav getKeys(){
		preHandle();
		List<PK> keys = JsonDatabeanTool.primaryKeysFromJson(
				fieldInfo.getPrimaryKeyClass(),
				fieldInfo.getSampleFielder().getKeyFielder(),
				params.required(HttpReaderNode.METHOD_getMulti_PARAM_keys));
		List<PK> existingKeys = node.getKeys(keys, config);
		JSON jsonExistingKeys = JsonDatabeanTool.primaryKeysToJson(existingKeys, fieldInfo.getSampleFielder()
				.getKeyFielder());
		return new JsonMav(jsonExistingKeys);
	}
	
}

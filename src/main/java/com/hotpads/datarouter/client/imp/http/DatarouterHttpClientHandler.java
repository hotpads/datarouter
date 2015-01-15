package com.hotpads.datarouter.client.imp.http;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.http.node.HttpReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Config.ConfigFielder;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderHttpNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.JsonMav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.ObjectTool;

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
	
	public static final boolean VERIFY_SIGNATURE = false;
	
	@Inject
	private DatarouterContext drContext;
	
	private String routerName;
	private String nodeName;
	private Datarouter router;
	private MapStorageReaderNode<PK,D> node;
	private DatabeanFieldInfo<PK,D,?> fieldInfo;
	private Config config;

	@Override
	@Handler
	protected Mav handleDefault(){
		return new MessageMav("hello");
	}
	
	private void preHandle(){
		authenticate();
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
	
	private void authenticate(){
		String drUsername = params.required(ApacheHttpClient.PARAMS_DR_USERNAME);
		if(ObjectTool.notEquals(ApacheHttpClient.DR_USERNAME, drUsername)){
			throw new IllegalArgumentException("invalid username:"+drUsername);
		}
		
		String drPassword = params.required(ApacheHttpClient.PARAMS_DR_PASSWORD);
		if(ObjectTool.notEquals(ApacheHttpClient.DR_PASSWORD, drPassword)){
			throw new IllegalArgumentException("invalid password:"+drPassword);
		}
		
		if(VERIFY_SIGNATURE){
			String uri = params.getRequest().getRequestURI();
			String signature = params.required(ApacheHttpClient.PARAMS_SIGNATURE);
			Map<String,String> requestParams = RequestTool.getMapOfParameters(params.getRequest());
			String expectedSignature = ApacheHttpClient.generateSignature(uri, requestParams, ApacheHttpClient.AUTH_SECRET);
			if(ObjectTool.notEquals(expectedSignature, signature)){
				throw new IllegalArgumentException("expected signature "+expectedSignature+" but received "+signature);
			}
		}
	}
	
	@Override
	@Handler
	public JsonMav exists(){
		preHandle();
		PK key = JsonDatabeanTool.primaryKeyFromJson(
				fieldInfo.getPrimaryKeyClass(),
				fieldInfo.getSampleFielder().getKeyFielder(),
				params.required(HttpReaderNode.METHOD_get_PARAM_key));
		D databean = node.get(key, config);
		JSONObject existsJson = new JSONObject();
		existsJson.put("exists", databean==null);
		return new JsonMav(existsJson);
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

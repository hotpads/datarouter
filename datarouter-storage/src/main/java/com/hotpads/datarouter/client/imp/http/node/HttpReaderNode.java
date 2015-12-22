package com.hotpads.datarouter.client.imp.http.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.http.DatarouterHttpClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Config.ConfigFielder;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.JsonDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class HttpReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalMapStorageReaderNode<PK,D>{

	/******************** static ****************************/

	public static final String
		ENCODING_json = "json",

		PARAM_config = "config",

		METHOD_get = "get",
		METHOD_get_PARAM_key = "key",

		METHOD_getAll = "getAll",

		METHOD_getMulti = "getMulti",
		METHOD_getMulti_PARAM_keys = "keys",

		METHOD_getKeys = "getKeys",
		METHOD_getKeys_PARAM_keys = "keys";


	/*************** fields ********************************/

	private ConfigFielder configFielder;

	private String remoteRouterName;
	private String remoteNodeName;


	/******************************* construct ************************************/

	public HttpReaderNode(NodeParams<PK,D,F> params) {
		super(params);
		this.configFielder = new ConfigFielder();
		this.remoteRouterName = params.getRemoteRouterName();
		this.remoteNodeName = params.getRemoteNodeName();
	}


	/***************************** plumbing methods ***********************************/

	@Override
	public DatarouterHttpClient getClient(){
		return (DatarouterHttpClient)getRouter().getClient(getClientId().getName());
	}

	/************************************ MapStorageReader methods ****************************/

	@Override
	public boolean exists(PK key, Config config) {
		return get(key, config) != null;
	}

	@Override
	public D get(final PK key, final Config config){
//		logger.warn("client get:"+key);
		if(key==null){
			return null;
		}

		Map<String,String> params = new HashMap<>();
		params.put(METHOD_get_PARAM_key, JsonDatabeanTool.primaryKeyToJson(key,
				fieldInfo.getSampleFielder().getKeyFielder()).toString());
		addConfigParam(params, config);

		StringBuilder uriBuilder = getOpUrl(METHOD_get);
		JSONObject jsonObject = getClient().getApacheHttpClient().request(params, uriBuilder.toString(),
				JSONObject.class);
		return JsonDatabeanTool.databeanFromJson(fieldInfo.getDatabeanSupplier(), fieldInfo.getSampleFielder(),
				jsonObject);
	}

	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config config){
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<>();
		}

		Map<String,String> params = new HashMap<>();
		params.put(METHOD_getMulti_PARAM_keys, JsonDatabeanTool.primaryKeysToJson(keys,
				fieldInfo.getSampleFielder().getKeyFielder()).toString());
		addConfigParam(params, config);

		StringBuilder uriBuilder = getOpUrl(METHOD_getMulti);
		JSONArray jsonArray = getClient().getApacheHttpClient().request(params, uriBuilder.toString(), JSONArray.class);
		return JsonDatabeanTool.databeansFromJson(fieldInfo.getDatabeanSupplier(), fieldInfo.getSampleFielder(),
				jsonArray);
	}

	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config) {
		if(DrCollectionTool.isEmpty(keys)){
			return new LinkedList<>();
		}

		Map<String,String> params = new HashMap<>();
		params.put(METHOD_getKeys_PARAM_keys, JsonDatabeanTool.primaryKeysToJson(keys,
				fieldInfo.getSampleFielder().getKeyFielder()).toString());
		addConfigParam(params, config);

		StringBuilder uriBuilder = getOpUrl(METHOD_getKeys);
		JSONArray jsonArray = getClient().getApacheHttpClient().request(params, uriBuilder.toString(), JSONArray.class);
		return JsonDatabeanTool.primaryKeysFromJson(fieldInfo.getPrimaryKeyClass(), fieldInfo.getSampleFielder()
				.getKeyFielder(), jsonArray);
	}


	/***************************** private *****************************/

	private void addConfigParam(Map<String,String> params, Config config){
		JSONObject json = JsonDatabeanTool.databeanToJson(config, configFielder);
		if(json==null){
			return;
		}
		params.put(PARAM_config, json.toString());
	}

	// should be like: /contextPath/datarouterApi/httpNode/routerName/clientName.nodeName
	private StringBuilder getNodeUrl(){
		StringBuilder sb = new StringBuilder();
		sb.append(getClient().getUrl());
		sb.append("?");
		sb.append("routerName="+remoteRouterName);
		sb.append("&");
		sb.append("nodeName="+remoteNodeName);
		return sb;
	}

	private StringBuilder getOpUrl(String opName){
		StringBuilder sb = getNodeUrl();
		sb.append("&");
		sb.append("submitAction="+opName);
		return sb;
	}

}

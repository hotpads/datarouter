package com.hotpads.datarouter.test.node.basic.map;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanEntity;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanEntityKey;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanKey;

public class MapStorageTestRouter extends BaseRouter{

	private static final String NAME = "MapStorageTestRouter";
	private static final int VERSION_mapStorageTestRouter = 1;

	private final List<ClientId> clientIds;

	private MapStorageNode<MapStorageBeanKey,MapStorageBean> mapStorageNode;

	public MapStorageTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId,
			DatarouterSettings datarouterSettings, boolean entity, EntityNodeFactory entityNodeFactory,
			EntityNodeParams<MapStorageBeanEntityKey,MapStorageBeanEntity> entityNodeParams){
		super(datarouter, DrTestConstants.CONFIG_PATH, NAME, nodeFactory, datarouterSettings);
		this.clientIds = Arrays.asList(clientId);

		if(entity){
			mapStorageNode = new MapStorageEntityNode(entityNodeFactory, nodeFactory, this, clientId, entityNodeParams)
					.mapStorageNode();
		}else{
			mapStorageNode = create(clientId, MapStorageBean::new, MapStorageBeanFielder::new)
				.withSchemaVersion(VERSION_mapStorageTestRouter)
				.buildAndRegister();
		}
	}

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}

	public MapStorageNode<MapStorageBeanKey,MapStorageBean> mapStorageNode(){
		return mapStorageNode;
	}
}
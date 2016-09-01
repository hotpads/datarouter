package com.hotpads.datarouter.test.node.basic.map;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageDatabean;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageDatabean.MapStorageDatabeanFielder;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageDatabeanKey;

public class MapStorageTestRouter extends BaseRouter{

	private static final String NAME = "MapStorageTestRouter";
	private static final int VERSION_mapStorageTestRouter = 1;

	private final List<ClientId> clientIds;

	private MapStorageNode<MapStorageDatabeanKey,MapStorageDatabean> mapStorageNode;

	public MapStorageTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId,
			DatarouterSettings datarouterSettings){
		super(datarouter, DrTestConstants.CONFIG_PATH, NAME, nodeFactory, datarouterSettings);
		this.clientIds = Arrays.asList(clientId);
		mapStorageNode = create(clientId, MapStorageDatabean::new, MapStorageDatabeanFielder::new)
			.withSchemaVersion(VERSION_mapStorageTestRouter)
			.buildAndRegister();
	}

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}

	public MapStorageNode<MapStorageDatabeanKey,MapStorageDatabean> mapStorageNode(){
		return mapStorageNode;
	}
}
package com.hotpads.datarouter.test.node.basic.map;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageDatabean;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageDatabean.MapStorageDatabeanFielder;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageDatabeanKey;

public class MapStorageTestRouter extends BaseRouter{

	private final List<ClientId> clientIds;
	private static final String NAME = "MapStorageTestRouter";
	private MapStorageNode<MapStorageDatabeanKey,MapStorageDatabean> mapStorageNode;

	public MapStorageTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId, boolean useFielder){
		super(datarouter, DrTestConstants.CONFIG_PATH, MapStorageTestRouter.class.getSimpleName(), nodeFactory, null);
		this.clientIds = Arrays.asList(clientId);

		Class<MapStorageDatabeanFielder> fielderClass = useFielder ? MapStorageDatabeanFielder.class : null;

		this.mapStorageNode = register(nodeFactory.create(clientId, MapStorageDatabean.class, fielderClass, 2, this,
				false));
	}

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}

	public MapStorageNode<MapStorageDatabeanKey,MapStorageDatabean> mapStorageNode(){
		return mapStorageNode;
	}
}

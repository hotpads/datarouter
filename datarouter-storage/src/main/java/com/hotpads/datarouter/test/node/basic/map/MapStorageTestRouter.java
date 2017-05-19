package com.hotpads.datarouter.test.node.basic.map;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.TestDatarouterProperties;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanEntity;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanEntityKey;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanKey;

public class MapStorageTestRouter extends BaseRouter{

	private static final String NAME = "MapStorageTestRouter";
	private static final int VERSION_mapStorageTestRouter = 1;

	public final MapStorageNode<MapStorageBeanKey,MapStorageBean> mapStorageNode;

	public MapStorageTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			NodeFactory nodeFactory, ClientId clientId, DatarouterSettings datarouterSettings, boolean entity,
			EntityNodeFactory entityNodeFactory,
			EntityNodeParams<MapStorageBeanEntityKey,MapStorageBeanEntity> entityNodeParams){
		super(datarouter, datarouterProperties.getDatarouterTestFileLocation(), NAME, nodeFactory,
				datarouterSettings);

		if(entity){
			mapStorageNode = new MapStorageEntityNode(entityNodeFactory, nodeFactory, this, clientId, entityNodeParams)
					.mapStorageNode();
		}else{
			mapStorageNode = create(clientId, MapStorageBean::new, MapStorageBeanFielder::new)
				.withSchemaVersion(VERSION_mapStorageTestRouter)
				.buildAndRegister();
		}
	}

}
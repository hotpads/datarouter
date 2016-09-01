package com.hotpads.datarouter.test.node.basic.map;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageDatabean;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageDatabeanKey;

public abstract class BaseMapStorageIntegrationTests{

	/** fields ***************************************************************/

	@Inject
	protected Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private DatarouterSettings datarouterSettings;

	protected MapStorageTestRouter router;
	private MapStorageNode<MapStorageDatabeanKey,MapStorageDatabean> mapStorageNode;

	/** setup/teardown *******************************************************/

	protected void setup(ClientId clientId){
		router = new MapStorageTestRouter(datarouter, nodeFactory, clientId, datarouterSettings);
		mapStorageNode = router.mapStorageNode();
	}

	/** tests ****************************************************************/

	@Test
	public void testGetMulti(){
		MapStorageDatabean bean0 = new MapStorageDatabean("key0", "data0");
		MapStorageDatabean bean1 = new MapStorageDatabean("key1", "data1");
		MapStorageDatabean bean2 = new MapStorageDatabean("key2", "data2");

		List<MapStorageDatabean> beans = new ArrayList<>();
		beans.add(bean0);
		beans.add(bean1);
		beans.add(bean2);
		mapStorageNode.putMulti(beans, null);

		List<MapStorageDatabeanKey> keys = new ArrayList<>();
		keys.add(bean0.getKey());
		keys.add(bean1.getKey());
		keys.add(bean2.getKey());

		List<MapStorageDatabean> roundTripped = mapStorageNode.getMulti(keys, null);

		Assert.assertTrue(roundTripped.contains(bean0));
		Assert.assertTrue(roundTripped.contains(bean1));
		Assert.assertTrue(roundTripped.contains(bean2));

		for(MapStorageDatabean bean : beans){
			deleteRecord(bean);
		}
	}

	@Test
	public void testPutMulti(){
		List<MapStorageDatabean> beans = new ArrayList<>();
		for(int i = 0; i < 10; i++){
			beans.add(new MapStorageDatabean("key " + i, "data " + i));
		}

		for(MapStorageDatabean bean : beans){
			Assert.assertFalse(mapStorageNode.exists(bean.getKey(), null));
		}

		mapStorageNode.putMulti(beans, null);

		for(MapStorageDatabean bean : beans){
			Assert.assertTrue(mapStorageNode.exists(bean.getKey(), null));
		}

		for(MapStorageDatabean bean : beans){
			deleteRecord(bean);
		}
	}


	/** private **************************************************************/

	private void deleteRecord(MapStorageDatabean bean){
		mapStorageNode.delete(bean.getKey(), null);
	}
}

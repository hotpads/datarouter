package com.hotpads.datarouter.test.node.basic.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanKey;

public abstract class BaseMapStorageIntegrationTests{

	/** fields ***************************************************************/

	@Inject
	protected Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private DatarouterSettings datarouterSettings;

	protected MapStorageTestRouter router;
	private MapStorageNode<MapStorageBeanKey,MapStorageBean> mapStorageNode;

	/** setup/teardown *******************************************************/

	protected void setup(ClientId clientId){
		router = new MapStorageTestRouter(datarouter, nodeFactory, clientId, datarouterSettings);
		mapStorageNode = router.mapStorageNode();
	}

	/** tests ****************************************************************/

	@Test
	protected void testGetMulti(){
		MapStorageBean bean0 = new MapStorageBean("data0");
		MapStorageBean bean1 = new MapStorageBean("data1");
		MapStorageBean bean2 = new MapStorageBean("data2");

		List<MapStorageBean> beans = new ArrayList<>();
		beans.add(bean0);
		beans.add(bean1);
		beans.add(bean2);
		mapStorageNode.putMulti(beans, null);

		List<MapStorageBeanKey> keys = new ArrayList<>();
		keys.add(bean0.getKey());
		keys.add(bean1.getKey());
		keys.add(bean2.getKey());

		List<MapStorageBean> roundTripped = mapStorageNode.getMulti(keys, null);

		Assert.assertTrue(roundTripped.contains(bean0));
		Assert.assertTrue(roundTripped.contains(bean1));
		Assert.assertTrue(roundTripped.contains(bean2));

		for(MapStorageBean bean : beans){
			deleteRecord(bean);
		}
	}

	@Test
	protected void testPutMulti(){
		List<MapStorageBean> beans = new ArrayList<>();
		for(int i = 0; i < 10; i++){
			beans.add(new MapStorageBean());
		}

		for(MapStorageBean bean : beans){
			Assert.assertFalse(mapStorageNode.exists(bean.getKey(), null));
		}

		mapStorageNode.putMulti(beans, null);

		for(MapStorageBean bean : beans){
			System.out.println("bean key: " + bean.getKey());
			Assert.assertTrue(mapStorageNode.exists(bean.getKey(), null));
		}

		for(MapStorageBean bean : beans){
			deleteRecord(bean);
		}
	}

	@Test
	protected void testBlankDatabeanPut(){
		Config config = new Config();
		MapStorageBean blankDatabean = new MapStorageBean(null);
		MapStorageBean nonBlankDatabean = new MapStorageBean("a");
		mapStorageNode.putMulti(Arrays.asList(nonBlankDatabean, blankDatabean), config);
		MapStorageBean blankDatabeanFromDb = mapStorageNode.get(blankDatabean.getKey(), config);
		new MapStorageBeanFielder().getNonKeyFields(blankDatabeanFromDb).stream()
				.map(Field::getValue)
				.forEach(AssertJUnit::assertNull);
		mapStorageNode.deleteMulti(DatabeanTool.getKeys(Arrays.asList(blankDatabean, nonBlankDatabean)), config);
		AssertJUnit.assertNull(mapStorageNode.get(blankDatabean.getKey(), config));
	}


	/** private **************************************************************/

	private void deleteRecord(MapStorageBean bean){
		mapStorageNode.delete(bean.getKey(), null);
	}
}

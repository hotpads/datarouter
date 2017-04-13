package com.hotpads.datarouter.test.node.basic.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.test.TestDatarouterProperties;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanKey;

public abstract class BaseMapStorageIntegrationTests{

	/** fields ***************************************************************/

	@Inject
	private TestDatarouterProperties datarouterProperties;
	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private DatarouterSettings datarouterSettings;
	@Inject
	private EntityNodeFactory entityNodeFactory;

	private MapStorageNode<MapStorageBeanKey,MapStorageBean> mapStorageNode;

	/** setup/teardown *******************************************************/

	protected void setup(ClientId clientId, boolean entity){
		mapStorageNode = new MapStorageTestRouter(datarouterProperties, datarouter, nodeFactory, clientId,
				datarouterSettings, entity, entityNodeFactory,
				MapStorageEntityNode.ENTITY_NODE_PARAMS_1).mapStorageNode;
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	/** tests ****************************************************************/

	@Test
	public void testGet(){
		List<MapStorageBean> beans = initBeans(1000);
		mapStorageNode.putMulti(beans, null);
		final int sampleEveryN = 29;
		for(int i = 0; i < beans.size(); i += sampleEveryN){
			MapStorageBean bean = beans.get(i);
			MapStorageBean roundTripped = mapStorageNode.get(bean.getKey(), null);
			Assert.assertEquals(roundTripped, bean);
		}
		deleteRecords(beans);
	}

	@Test
	public void testGetMulti(){
		List<MapStorageBean> beans = initBeans(10);
		mapStorageNode.putMulti(beans, null);

		List<MapStorageBeanKey> keys = DatabeanTool.getKeys(beans);
		List<MapStorageBean> roundTripped = mapStorageNode.getMulti(keys, null);
		Assert.assertEquals(roundTripped.size(), beans.size());

		for(MapStorageBean bean : beans){
			Assert.assertTrue(roundTripped.contains(bean));
		}

		deleteRecords(beans);
	}

	@Test
	public void testPutMulti(){
		List<MapStorageBean> beans = initBeans(10);

		for(MapStorageBean bean : beans){
			Assert.assertFalse(mapStorageNode.exists(bean.getKey(), null));
		}

		mapStorageNode.putMulti(beans, null);

		for(MapStorageBean bean : beans){
			Assert.assertTrue(mapStorageNode.exists(bean.getKey(), null));
		}

		deleteRecords(beans);
	}

	@Test
	public void testBlankDatabeanPut(){
		MapStorageBean blankDatabean = new MapStorageBean(null);
		MapStorageBean nonBlankDatabean = new MapStorageBean("a");
		mapStorageNode.putMulti(Arrays.asList(nonBlankDatabean, blankDatabean), null);
		MapStorageBean roundTrippedBlank = mapStorageNode.get(blankDatabean.getKey(), null);
		new MapStorageBeanFielder().getNonKeyFields(roundTrippedBlank).stream()
				.map(Field::getValue)
				.forEach(Assert::assertNull);
		mapStorageNode.deleteMulti(DatabeanTool.getKeys(Arrays.asList(blankDatabean, nonBlankDatabean)), null);
		Assert.assertNull(mapStorageNode.get(blankDatabean.getKey(), null));
	}


	@Test
	public void testGetKeys(){
		MapStorageBean bean0 = new MapStorageBean();
		MapStorageBean bean1 = new MapStorageBean(); // not inserted to db
		MapStorageBean bean2 = new MapStorageBean();

		mapStorageNode.put(bean0, null);
		mapStorageNode.put(bean2, null);

		List<MapStorageBeanKey> keysToGet = Arrays.asList(bean0.getKey(), bean1.getKey(), bean2.getKey());
		List<MapStorageBeanKey> keysGotten = mapStorageNode.getKeys(keysToGet, null);

		Assert.assertTrue(keysGotten.contains(bean0.getKey()));
		Assert.assertFalse(keysGotten.contains(bean1.getKey()));
		Assert.assertTrue(keysGotten.contains(bean2.getKey()));

		deleteRecord(bean0);
		deleteRecord(bean2);
	}

	/** private **************************************************************/

	private List<MapStorageBean> initBeans(int size){
		List<MapStorageBean> beans = new ArrayList<>();
		for(int i = 0; i < size; i++){
			beans.add(new MapStorageBean("data" + i));
		}
		return beans;
	}

	/** remove records *******************************************************/

	private void deleteRecord(MapStorageBean bean){
		mapStorageNode.delete(bean.getKey(), null);
	}

	private void deleteRecords(List<MapStorageBean> beans){
		for(MapStorageBean bean : beans){
			deleteRecord(bean);
		}
	}
}

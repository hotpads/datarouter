/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.hbase.test.nonentity;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.client.hbase.node.nonentity.HBaseNode;
import io.datarouter.client.hbase.test.DatarouterHBaseTestClientIds;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.test.node.basic.manyfield.BaseManyFieldIntegrationTests;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBeanKey;
import io.datarouter.util.collection.MapTool;

@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HBaseManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterHBaseTestClientIds.HBASE, ManyFieldTypeBeanFielder::new);
	}

	@Test
	public void testIncrement(){
		@SuppressWarnings("unchecked")
		HBaseNode<ManyFieldBeanKey,?,ManyFieldBeanKey,ManyFieldBean,?> hbaseNode
				= (HBaseNode<ManyFieldBeanKey,?,ManyFieldBeanKey,ManyFieldBean,?>)NodeTool
				.extractSinglePhysicalNode(dao.getNode());
		ManyFieldBean bean = new ManyFieldBean();

		//increment by 3
		Map<ManyFieldBeanKey,Map<String,Long>> increments = new HashMap<>();
		MapTool.increment(increments, bean.getKey(), ManyFieldBean.FieldKeys.incrementField.getName(), 30L);
		hbaseNode.increment(increments, new Config());
		ManyFieldBean result1 = dao.get(bean.getKey());
		Assert.assertEquals(result1.getIncrementField(), Long.valueOf(30));

		//decrement by 11
		increments.clear();
		MapTool.increment(increments, bean.getKey(), ManyFieldBean.FieldKeys.incrementField.getName(), -11L);
		hbaseNode.increment(increments, new Config());
		ManyFieldBean result2 = dao.get(bean.getKey());
		Assert.assertEquals(result2.getIncrementField(), Long.valueOf(19));

		//increment by 17 (expecting 30 - 11 + 17 => 36)
		increments.clear();
		MapTool.increment(increments, bean.getKey(), ManyFieldBean.FieldKeys.incrementField.getName(), 17L);
		hbaseNode.increment(increments, new Config());
		ManyFieldBean result3 = dao.get(bean.getKey());
		Assert.assertEquals(result3.getIncrementField(), Long.valueOf(36));
	}

}

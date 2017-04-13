package com.hotpads.datarouter.test;

import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBeanKey;
import com.hotpads.datarouter.util.core.DrMapTool;

public class HBaseManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.hbase);
	}

	@Override
	public boolean isHBase(){
		return true;
	}


	@Test
	public void testIncrement(){
		if(!isHBase()){
			return;
		}
		@SuppressWarnings("unchecked")
		HBaseNode<ManyFieldBeanKey,ManyFieldBean,?> hbaseNode = (HBaseNode<ManyFieldBeanKey,ManyFieldBean,?>) mapNode
				.getPhysicalNodeIfApplicable();
		ManyFieldBean bean = new ManyFieldBean();

		//increment by 3
		Map<ManyFieldBeanKey,Map<String,Long>> increments = new HashMap<>();
		DrMapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, 30L);
		hbaseNode.increment(increments, null);
		ManyFieldBean result1 = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(new Long(30), result1.getIncrementField());

		//decrement by 11
		increments.clear();
		DrMapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, -11L);
		hbaseNode.increment(increments, null);
		ManyFieldBean result2 = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(new Long(19), result2.getIncrementField());

		//increment by 17 (expecting 30 - 11 + 17 => 36)
		increments.clear();
		DrMapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, 17L);
		hbaseNode.increment(increments, null);
		ManyFieldBean result3 = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(new Long(36), result3.getIncrementField());

		recordKey(bean.getKey());
	}
}

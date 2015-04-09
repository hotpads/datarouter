package com.hotpads.datarouter.client.imp.hbase.test;

import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBeanKey;
import com.hotpads.datarouter.util.core.DrMapTool;

public class HBaseManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHBase, true);
	}

	@Override
	public boolean isHBase(){
		return true;
	}

	
	@Test
	public void testIncrement(){
		if(!isHBase()){ return; }
		@SuppressWarnings("unchecked")
		HBaseNode<ManyFieldBeanKey, ManyFieldBean, ?> hBaseNode = (HBaseNode<ManyFieldBeanKey,ManyFieldBean,?>)mapNode
				.getPhysicalNodeIfApplicable();
		ManyFieldBean bean = new ManyFieldBean();

		//increment by 3
		Map<ManyFieldBeanKey,Map<String,Long>> increments = new HashMap<>();
		DrMapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, 3L);
		hBaseNode.increment(increments, null);
		ManyFieldBean result1 = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(new Long(3), result1.getIncrementField());

		//decrement by 11
		increments.clear();
		DrMapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, -11L);
		hBaseNode.increment(increments, null);
		ManyFieldBean result2 = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(new Long(-8), result2.getIncrementField());

		//increment by 17 (expecting 3 - 11 + 17 => 9)
		increments.clear();
		DrMapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, 17L);
		hBaseNode.increment(increments, null);
		ManyFieldBean result3 = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(new Long(9), result3.getIncrementField());

		recordKey(bean.getKey());
	}
}

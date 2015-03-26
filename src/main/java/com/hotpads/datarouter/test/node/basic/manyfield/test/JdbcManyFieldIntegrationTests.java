package com.hotpads.datarouter.test.node.basic.manyfield.test;

import javax.inject.Inject;

import org.junit.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.google.inject.Injector;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.test.TestDatarouterInjectorProvider;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBeanKey;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTestRouter;

public class JdbcManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestJdbc0, true);
	}

	@Override
	public boolean isJdbc(){
		return true;
	}
	
	@Guice(moduleFactory = DatarouterTestModuleFactory.class)
	public static class TableRowCountIntegrationTests{
		
		@Inject
		private DatarouterContext drContext;
	
		public void test(){
			System.out.println("integration test");
			System.out.println(drContext.getRouters());
		}
		
	}
	
	@Test
	public void test(){	
		
		Injector injector = new TestDatarouterInjectorProvider().get();
		DatarouterContext drContext = injector.getInstance(DatarouterContext.class);
		System.out.println(drContext.getRouters());
		//ConfigRouter configRouter = injector.getInstance(ConfigRouter.class);
		
		MapStorageNode<ManyFieldBeanKey,ManyFieldBean> mapNode;
	
		ManyFieldBean bean = new ManyFieldBean(1L);
		ManyFieldBeanKey key = new ManyFieldBeanKey(1L);
		System.out.println(key.getPrimaryKey());
		byte[] byteVal = FieldTool.getConcatenatedValueBytes(key.getFields(), false, false);
		System.out.println("byteval"+byteVal);
		 bean.setByteArrayField(byteVal);
		 
		 
		//Injector injector = new TestDatarouterInjectorProvider().get();
		//DatarouterContext drContext = injector.getInstance(DatarouterContext.class);
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		String clientName = "place";
		
		ManyFieldTestRouter router = new ManyFieldTestRouter(drContext, nodeFactory, clientName, false);
		mapNode = router.manyFieldTypeBean();
		 mapNode.put(bean, null);
		 System.out.println("inserted");
		/*
		 bean.setByteArrayField(byteVal);
		System.out.println("byte array testing");
		mapNode.put(bean, null);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		System.out.println("bean"+bean.getByteArrayField());
		System.out.println("roundtrip"+roundTripped.getByteArrayField());
		 */
		
	//	this.testByteArray();
	}
}

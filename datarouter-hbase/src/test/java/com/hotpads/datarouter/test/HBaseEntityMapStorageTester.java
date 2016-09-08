package com.hotpads.datarouter.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.test.node.basic.map.BaseMapStorageIntegrationTests;

@Guice(moduleFactory=DatarouterStorageTestModuleFactory.class)
public class HBaseEntityMapStorageTester extends BaseMapStorageIntegrationTests{

	// TODO Debug this. The router/node is not storing any beans in hbase (entity).
	// Could be issue with MapStorageBeanEntity, MapStorageBeanEntityKey or MapStorageEntityNode

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHBase, true);
	}
}
package com.hotpads.datarouter.client.imp.hbase.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.backup.test.BackupIntegrationTester;

public class HBaseBackupIntegrationTester extends BackupIntegrationTester{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHBase);
	}
	
}

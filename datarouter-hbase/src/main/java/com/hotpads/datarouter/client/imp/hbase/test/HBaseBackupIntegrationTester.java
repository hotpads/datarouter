package com.hotpads.datarouter.client.imp.hbase.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.backup.test.BackupIntegrationTester;

public class HBaseBackupIntegrationTester extends BackupIntegrationTester{

	@BeforeClass
	public void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHBase, true);
	}
	
}

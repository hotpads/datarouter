package com.hotpads.datarouter.node.op.raw.write;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.test.node.basic.backup.BackupBean;
import com.hotpads.util.wal.WriteAheadLog;
import com.hotpads.util.wal.imp.file.FileRollingWal;

public class WalEncoderTester{
	WalEncoder walEncoder;
	WriteAheadLog wal;
	private String directoryPath = "/tmp/WAL-tests/WalEncoder/";
	@Before
	public void setUp() throws Exception{
		wal = new FileRollingWal(directoryPath);
		walEncoder = new WalEncoder(wal);
	}

	@After
	public void tearDown() throws Exception{
		wal.delete();
	}

//	@Test
//	public void test(){
//		Databean<?,?> databean = new BackupBean("String","String2",123,"String3","String4",1L,"String5",3D);
//		Config config = null;
//		walEncoder.put(databean, config);
//	}

}

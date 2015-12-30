package com.hotpads.datarouter.test.node.basic.backup.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.backup.BackupRegion;
import com.hotpads.datarouter.backup.imp.memory.BackupRegionToMemory;
import com.hotpads.datarouter.backup.imp.memory.RestoreRegionFromMemory;
import com.hotpads.datarouter.backup.imp.s3.BackupRegionToS3;
import com.hotpads.datarouter.backup.imp.s3.RestoreRegionFromS3;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.test.node.basic.backup.BackupBean;
import com.hotpads.datarouter.test.node.basic.backup.BackupBeanKey;
import com.hotpads.datarouter.test.node.basic.backup.BackupTestRouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeans;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.profile.PhaseTimer;

@Guice(moduleFactory=DatarouterTestModuleFactory.class)
public class BackupIntegrationTester{

	/***************************** fields **************************************/

	private static final Supplier<BackupBean> BACKUP_BEAN_SUPPLIER = ReflectionTool.supplier(BackupBean.class);

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	protected ClientType clientType;
	protected BackupTestRouter router;

	/***************************** construct **************************************/

	public void setup(ClientId clientId){
		this.router = new BackupTestRouter(datarouter, nodeFactory, clientId);
	}


	/****************************** dummy data *************************************/

	public void resetTable(){
		clearTable(router);

		List<String> as = DrListTool.createArrayList(STRINGS);
		List<String> bs = DrListTool.createArrayList(STRINGS);
		List<Integer> cs = DrListTool.createArrayList(INTEGERS);
		List<String> ds = DrListTool.createArrayList(STRINGS);
		Collections.shuffle(as);
		Collections.shuffle(bs);
		Collections.shuffle(cs);
		Collections.shuffle(ds);

		List<BackupBean> toSave = new ArrayList<>();
		for(int a=0; a < NUM_ELEMENTS; ++a){
			for(int b=0; b < NUM_ELEMENTS; ++b){
				for(int c=0; c < NUM_ELEMENTS; ++c){
					for(int d=0; d < NUM_ELEMENTS; ++d){
						BackupBean bean = new BackupBean(
								as.get(a), bs.get(b), cs.get(c), ds.get(d),
								"string so hbase has at least one field", null, null, null);
						toSave.add(bean);
					}
				}
			}
		}
		router.backupBeanNode().putMulti(toSave,
				new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		Assert.assertEquals(TOTAL_RECORDS, DrIterableTool.count(router.backupBeanNode().scan(null, null)).intValue());
	}

	public static void clearTable(BackupTestRouter routerToReset){
		routerToReset.backupBeanNode().deleteAll(null);
		Assert.assertEquals(0, DrIterableTool.count(routerToReset.backupBeanNode().scan(null, null)).intValue());
	}


	/****************************** testing vars ***********************************/

	public static final SortedSet<String> STRINGS = SortedBeans.STRINGS;
	public static final int NUM_ELEMENTS = SortedBeans.NUM_ELEMENTS;
	public static final List<Integer> INTEGERS = SortedBeans.INTEGERS;
	public static final int TOTAL_RECORDS = SortedBeans.TOTAL_RECORDS;

	/********************** junit methods  *********************************************/

	@Test
	public synchronized void testRoundTripMemory() throws IOException{
		resetTable();
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		BackupRegionToMemory<BackupBeanKey,BackupBean> backup = new BackupRegionToMemory<>(node,
				BackupRegion.DATABEAN_CONFIG, null, null, null, false);
		backup.execute();

		//clear table
		clearTable(router);

		//now read back in
		RestoreRegionFromMemory<BackupBeanKey,BackupBean> restore = new RestoreRegionFromMemory<>(backup.getResult(),
				ReflectionTool.supplier(BackupBean.class), router, node, false);
		restore.call();
		Assert.assertEquals(TOTAL_RECORDS, DrIterableTool.count(node.scan(null, null)).intValue());
	}

	//basically a debug test
	@Test
	public synchronized void testCompressedRoundTripMemory() throws IOException{
		resetTable();
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		BackupRegionToMemory<BackupBeanKey,BackupBean> backup = new BackupRegionToMemory<>(node,
				BackupRegion.DATABEAN_CONFIG, null, null, null, true);
		backup.execute();

		//clear table
		clearTable(router);
		//now read back in
		RestoreRegionFromMemory<BackupBeanKey,BackupBean> restore = new RestoreRegionFromMemory<>(
				backup.getResult(), BACKUP_BEAN_SUPPLIER, router, node, true);
		restore.call();
		Assert.assertEquals(TOTAL_RECORDS, DrIterableTool.count(node.scan(null, null)).intValue());
	}

	//basically a debug test
	@Test
	public synchronized void testManualDecompressMemory() throws IOException{
		resetTable();
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		BackupRegionToMemory<BackupBeanKey,BackupBean> backup = new BackupRegionToMemory<>(node,
				BackupRegion.DATABEAN_CONFIG, null, null, null, true);
		backup.execute();
		byte[] compressedBytes = backup.getResult();

		ByteArrayInputStream compressedIs = new ByteArrayInputStream(compressedBytes);
		GZIPInputStream gzipIs = new GZIPInputStream(compressedIs, BackupRegion.GZIP_BUFFER_BYTES);
		ByteArrayOutputStream uncompressedOs = new ByteArrayOutputStream();
		while(true){
			int byteRead = gzipIs.read();
			if(byteRead < 0){
				break;
			}
			uncompressedOs.write(byteRead);
		}
		byte[] roundTripped = uncompressedOs.toByteArray();

		//clear table
		clearTable(router);

		RestoreRegionFromMemory<BackupBeanKey,BackupBean> restore = new RestoreRegionFromMemory<>(
				roundTripped, BACKUP_BEAN_SUPPLIER, router, node, false);
		restore.call();
		Assert.assertEquals(TOTAL_RECORDS, DrIterableTool.count(node.scan(null, null)).intValue());
	}

	//basically a debug test
	//trying to figure out why the other tests sometimes fail
	@Test
	public synchronized void testGzipRoundTrip() throws IOException{
		resetTable();
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();

		BackupRegionToMemory<BackupBeanKey,BackupBean> uncompressedBackup = new BackupRegionToMemory<>(node,
				BackupRegion.DATABEAN_CONFIG, null, null, null, false);
		uncompressedBackup.execute();
		byte[] uncompressedBytes = uncompressedBackup.getResult();
		ByteArrayInputStream uncompressedIs = new ByteArrayInputStream(uncompressedBytes);
		ByteArrayOutputStream compressedOs = new ByteArrayOutputStream();
		GZIPOutputStream gzipOs = new GZIPOutputStream(compressedOs, BackupRegion.GZIP_BUFFER_BYTES);
		while(true){
			int byteRead = uncompressedIs.read();
			if(byteRead < 0){
				break;
			}
			gzipOs.write(byteRead);
		}
		gzipOs.close();
		byte[] compressedBytes = compressedOs.toByteArray();
		Assert.assertTrue(compressedBytes.length > 0);
		Assert.assertTrue(compressedBytes.length < uncompressedBytes.length);

		ByteArrayInputStream compressedIs = new ByteArrayInputStream(compressedBytes);
		GZIPInputStream gzipIs = new GZIPInputStream(compressedIs, BackupRegion.GZIP_BUFFER_BYTES);
		ByteArrayOutputStream uncompressedOs = new ByteArrayOutputStream();
		while(true){
			int byteRead = gzipIs.read();
			if(byteRead < 0){
				break;
			}
			uncompressedOs.write(byteRead);
		}
		byte[] roundTripped = uncompressedOs.toByteArray();
		Assert.assertEquals(uncompressedBytes, roundTripped);
	}

	@Test
	public synchronized void testRoundTripS3(){
		resetTable();
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		boolean gzip = true;
		String startKey = null;
		String endKey = null;
		new BackupRegionToS3<>(BackupRegionToS3.DEFAULT_BUCKET, "test", router, node,
				"testMigrationFolder", new Config(), startKey, endKey, null,
				Long.MAX_VALUE, gzip, true, new PhaseTimer()).execute();

		//clear table
		clearTable(router);

		//now read back in
		String s3Key = BackupRegionToS3.makeS3Key("testMigrationFolder","test", router, node);
		new RestoreRegionFromS3<>(BackupRegionToS3.DEFAULT_BUCKET, s3Key, BACKUP_BEAN_SUPPLIER, router, node, 100,
				false, true, gzip, true).call();
		Assert.assertEquals(TOTAL_RECORDS, DrIterableTool.count(node.scan(null, null)).intValue());
	}

}

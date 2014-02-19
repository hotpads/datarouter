package com.hotpads.datarouter.test.node.basic.backup.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hotpads.datarouter.backup.BackupRegion;
import com.hotpads.datarouter.backup.imp.memory.BackupRegionToMemory;
import com.hotpads.datarouter.backup.imp.memory.RestoreRegionFromMemory;
import com.hotpads.datarouter.backup.imp.s3.BackupRegionToS3;
import com.hotpads.datarouter.backup.imp.s3.RestoreRegionFromS3;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter.SortedBasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.backup.BackupBean;
import com.hotpads.datarouter.test.node.basic.backup.BackupBeanKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.profile.PhaseTimer;

@RunWith(Parameterized.class)
public class BackupIntegrationTests{
	static Logger logger = Logger.getLogger(BackupIntegrationTests.class);
	
	/****************************** client types ***********************************/

	public static List<ClientType> clientTypes = ListTool.create();
	public static List<Object[]> clientTypeObjectArrays = ListTool.create();
	static{
		clientTypes.add(HibernateClientType.INSTANCE);
		clientTypes.add(HBaseClientType.INSTANCE);
		for(ClientType clientType : clientTypes){
			clientTypeObjectArrays.add(new Object[]{clientType});
		}
	}
	
	/************************************ routers ***************************************/

	static Map<ClientType,SortedBasicNodeTestRouter> routerByClientType = MapTool.create();
	
	@BeforeClass
	public static void init() throws IOException{	
		Class<?> cls = BackupIntegrationTests.class;

		if(clientTypes.contains(HibernateClientType.INSTANCE)){
			routerByClientType.put(
					HibernateClientType.INSTANCE, 
					new SortedBasicNodeTestRouter(DRTestConstants.CLIENT_drTestHibernate0, cls));
		}

		if(clientTypes.contains(HBaseClientType.INSTANCE)){
			routerByClientType.put(
					HBaseClientType.INSTANCE, 
					new SortedBasicNodeTestRouter(DRTestConstants.CLIENT_drTestHBase, cls));
		}
		
		for(BasicNodeTestRouter router : routerByClientType.values()){
			resetTable(router);
		}
	}
	
	/****************************** dummy data *************************************/
	
	public static void resetTable(BasicNodeTestRouter routerToReset){	
		clearTable(routerToReset);
		
		List<String> as = ListTool.createArrayList(STRINGS);
		List<String> bs = ListTool.createArrayList(STRINGS);
		List<Integer> cs = ListTool.createArrayList(INTEGERS);
		List<String> ds = ListTool.createArrayList(STRINGS);
		Collections.shuffle(as);
		Collections.shuffle(bs);
		Collections.shuffle(cs);
		Collections.shuffle(ds);
		
		List<BackupBean> toSave = ListTool.createArrayList();
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
		routerToReset.backupBeanNode().putMulti(toSave, 
				new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(routerToReset.backupBeanNode().getAll(null)));
	}
	
	public static void clearTable(BasicNodeTestRouter routerToReset){
		routerToReset.backupBeanNode().deleteAll(null);
		Assert.assertEquals(0, CollectionTool.size(routerToReset.backupBeanNode().getAll(null)));
	}
	
	/***************************** fields **************************************/
	
	protected ClientType clientType;
	protected BasicNodeTestRouter router;

	/***************************** constructors **************************************/
	
	@Parameters
	public static Collection<Object[]> parameters(){//tests repeat for each Object[]
		return clientTypeObjectArrays;
	}
	
	public BackupIntegrationTests(ClientType clientType){//passed in by junit from the "parameters"
		this.clientType = clientType;
		this.router = routerByClientType.get(clientType);
	}
	
	
	/****************************** testing vars ***********************************/
	
	public static final String 
			S_aardvark = "aardvark",
			S_albatross = "albatross",
			S_alpaca = "alpaca",
			S_chinchilla = "chinchilla",
			S_emu = "emu",
			S_gopher = "gopher",
			S_ostrich = "ostrich",
			S_pelican = "pelican";
	
	public static final SortedSet<String> STRINGS = SetTool.createTreeSet(
			S_aardvark,
			S_albatross,
			S_alpaca,
			S_chinchilla,
			S_emu,
			S_gopher,
			S_ostrich,
			S_pelican);

	public static final String PREFIX_a = "a";
	public static final int NUM_PREFIX_a = 3;

	public static final String PREFIX_ch = "ch";
	public static final int NUM_PREFIX_ch = 1;

	public static final	String 
			RANGE_al = "al",
			RANGE_alp = "alp",
			RANGE_emu = "emu";
	
	public static final int 
			RANGE_LENGTH_alp = 6,
			RANGE_LENGTH_al_b = 2,
			RANGE_LENGTH_alp_emu_inc = 3,//exclude things that begin with emu without the other 3 key fields
			RANGE_LENGTH_emu = 4;
	
	public static final int NUM_ELEMENTS = STRINGS.size();
	public static final List<Integer> INTEGERS = ListTool.createArrayList(NUM_ELEMENTS);
	static{
		for(int i=0; i < NUM_ELEMENTS; ++i){
			INTEGERS.add(i);
		}
	}
	
	public static final int TOTAL_RECORDS = NUM_ELEMENTS*NUM_ELEMENTS*NUM_ELEMENTS*NUM_ELEMENTS;
		
	/********************** junit methods  *********************************************/
	
	@Test public synchronized void testRoundTripMemory() throws IOException{
		resetTable(router);
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		BackupRegionToMemory<BackupBeanKey,BackupBean> backup = new BackupRegionToMemory<BackupBeanKey,BackupBean>(
				router, node, null, null, false, 
				router.backupRecordNode());
		backup.execute();
		
		//clear table
		clearTable(router);
		
		//now read back in
		RestoreRegionFromMemory<BackupBeanKey,BackupBean> restore = new RestoreRegionFromMemory<BackupBeanKey,BackupBean>(
				backup.getResult(), BackupBean.class, router, node, false);
		restore.call();
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(node.getAll(null)));
	}

	//basically a debug test
	@Test public synchronized void testCompressedRoundTripMemory() throws IOException{
		resetTable(router);
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		BackupRegionToMemory<BackupBeanKey,BackupBean> backup = new BackupRegionToMemory<BackupBeanKey,BackupBean>(
				router, node, null, null, true,
				router.backupRecordNode());
		backup.execute();
		byte[] compressedBytes = backup.getResult();
		
		//clear table
		clearTable(router);
		//now read back in
		RestoreRegionFromMemory<BackupBeanKey,BackupBean> restore = new RestoreRegionFromMemory<BackupBeanKey,BackupBean>(
				backup.getResult(), BackupBean.class, router, node, true);
		restore.call();
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(node.getAll(null)));
	}

	//basically a debug test
	@Test public synchronized void testManualDecompressMemory() throws IOException{
		resetTable(router);
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		BackupRegionToMemory<BackupBeanKey,BackupBean> backup = new BackupRegionToMemory<BackupBeanKey,BackupBean>(
				router, node, null, null, true,
				router.backupRecordNode());
		backup.execute();
		byte[] compressedBytes = backup.getResult();
		
		ByteArrayInputStream compressedIs = new ByteArrayInputStream(compressedBytes);
		GZIPInputStream gzipIs = new GZIPInputStream(compressedIs, BackupRegion.GZIP_BUFFER_BYTES);
		ByteArrayOutputStream uncompressedOs = new ByteArrayOutputStream();
		while(true){
			int b = gzipIs.read();
			if(b < 0){ break; }
			uncompressedOs.write(b);
		}
		byte[] roundTripped = uncompressedOs.toByteArray();
		
		//clear table
		clearTable(router);
		
		RestoreRegionFromMemory<BackupBeanKey,BackupBean> restore = new RestoreRegionFromMemory<BackupBeanKey,BackupBean>(
				roundTripped, BackupBean.class, router, node, false);
		restore.call();
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(node.getAll(null)));
	}
	
	//basically a debug test
	//trying to figure out why the other tests sometimes fail
	@Test public synchronized void testGZIPRoundTrip() throws IOException{
		resetTable(router);
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		
		BackupRegionToMemory<BackupBeanKey,BackupBean> uncompressedBackup = new BackupRegionToMemory<BackupBeanKey,BackupBean>(
				router, node, null, null, false,
				router.backupRecordNode());
		uncompressedBackup.execute();
		byte[] uncompressedBytes = uncompressedBackup.getResult();
		ByteArrayInputStream uncompressedIs = new ByteArrayInputStream(uncompressedBytes);
		ByteArrayOutputStream compressedOs = new ByteArrayOutputStream();
		GZIPOutputStream gzipOs = new GZIPOutputStream(compressedOs, BackupRegion.GZIP_BUFFER_BYTES);
		while(true){
			int b = uncompressedIs.read();
			if(b < 0){ break; }
			gzipOs.write(b);
		}
		gzipOs.close();
		byte[] compressedBytes = compressedOs.toByteArray();
		Assert.assertTrue(compressedBytes.length > 0);
		Assert.assertTrue(compressedBytes.length < uncompressedBytes.length);
		
		ByteArrayInputStream compressedIs = new ByteArrayInputStream(compressedBytes);
		GZIPInputStream gzipIs = new GZIPInputStream(compressedIs, BackupRegion.GZIP_BUFFER_BYTES);
		ByteArrayOutputStream uncompressedOs = new ByteArrayOutputStream();
		while(true){
			int b = gzipIs.read();
			if(b < 0){ break; }
			uncompressedOs.write(b);
		}
		byte[] roundTripped = uncompressedOs.toByteArray();
		Assert.assertArrayEquals(uncompressedBytes, roundTripped);
	}
	
	@Test public synchronized void testRoundTripS3() throws IOException{
		resetTable(router);
		SortedMapStorageNode<BackupBeanKey,BackupBean> node = router.backupBeanNode();
		boolean gzip = true;
		new BackupRegionToS3<BackupBeanKey,BackupBean>(
				BackupRegionToS3.DEFAULT_BUCKET, "test", router, node, null, null, gzip, true,
				router.backupRecordNode(), new PhaseTimer()).execute();
		//clear table
		clearTable(router);
		
		//now read back in
		String s3Key = BackupRegionToS3.getS3Key("test", router, node);
		new RestoreRegionFromS3<BackupBeanKey,BackupBean>(
				BackupRegionToS3.DEFAULT_BUCKET, s3Key, BackupBean.class, router, node, 100, false, true, gzip, true).call();
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(node.getAll(null)));
	}
	
}





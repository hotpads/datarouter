package com.hotpads.datarouter.test.client.pool;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hbase.pool.HTableExecutorServicePool;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.ByteField;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.number.RandomTool;

public class HTableExecutorServicePoolTester {
	Logger logger = Logger.getLogger(HTableExecutorServicePoolTester.class);

	static BasicClientTestRouter router;
	static HBaseClientImp client;
	static HTableExecutorServicePool pool;
	static MapStorage<PoolTestBeanKey,PoolTestBean> node;
	static String tableName;

	public static void warmUp(){
//		node.deleteAll(null);
//		Assert.assertEquals(0, CollectionTool.size(node.getAll(null)));
		node.put(new PoolTestBean(12345L), null);
	}

	@BeforeClass
	public static void init() throws IOException{
		Injector injector = Guice.createInjector();
		router = injector.getInstance(BasicClientTestRouter.class);
		client = (HBaseClientImp)router.getClient(DRTestConstants.CLIENT_drTestHBase);
		//yes, this test will fail if we change the pool type
		pool = (HTableExecutorServicePool)client.getHTablePool();
		node = router.poolTestBeanHBase();
		tableName = PoolTestBean.class.getSimpleName();
		warmUp();
	}


	/************ InsertRollback *********************/

	@Test
	public void testPoolMonitoring(){
		ThreadPoolExecutor exec = new ThreadPoolExecutor(30,
				30,
				60,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
				new ThreadPoolExecutor.CallerRunsPolicy());

		int npes=0, toes=0;

		Random random = new Random();
		List<Future> futures = ListTool.createArrayList();
		for(int i=0; i < 1000000; ++i) {
			long randomLong = RandomTool.nextPositiveLong(random);
			ActionUsingPool task = new ActionUsingPool(randomLong);
			futures.add(exec.submit(task));
		}
		for(int i=0; i < 1000000; ++i) {
			Future<Void> future = futures.get(i);
			try{
//				future.get(3, TimeUnit.SECONDS);
				future.get();
//			} catch(TimeoutException e){
//				e.printStackTrace();
			} catch(RuntimeException e){
				e.printStackTrace();
			} catch(ExecutionException e){
				if(e.getCause() instanceof DataAccessException) {
					DataAccessException purposefulException = (DataAccessException)e.getCause();
					if(purposefulException.getCause() instanceof TimeoutException) {
						++toes;
					}else if(purposefulException.getCause() instanceof NullPointerException) {
						++npes;
					}
				}else {
					e.printStackTrace();
				}
			} catch(InterruptedException e){
				e.printStackTrace();
			}

//			try {
//				FutureTool.get(future);
//			}catch(RuntimeException e) {
//				e.printStackTrace();
//			}

			if(i % 10000 == 0) {
				logger.warn("did "+i+", NPEs:"+npes+", TOEs:"+toes);
			}
		}
		exec.shutdownNow();
	}

	class ActionUsingPool implements Callable<Void>{
		long randomLong;
		public ActionUsingPool(long randomLong){
			this.randomLong = randomLong;
		}


		@Override
		public Void call(){
			HTable hTable = null;
			boolean possiblyTarnishedHTable = false;
			try{
				hTable = client.checkOutHTable(tableName);
				return hbaseCall(hTable);
			}catch(Exception e){
				possiblyTarnishedHTable = true;
				throw new DataAccessException(e);
			}finally{
				if(hTable==null){
					logger.warn("not checking in HTable because it's null");
				}else if(client==null){
					logger.warn("not checking in HTable because client is null");
				}else{
					client.checkInHTable(hTable, possiblyTarnishedHTable);
				}
				pool.forceLogIfInconsistentCounts(false, tableName);
			}
		}

		protected Void hbaseCall(HTable hTable) throws TimeoutException, InterruptedException, IOException{
			if(eventMod10(0)) {
				throw new NullPointerException();
			}
			else if(eventMod10(1)) {
				Thread.sleep(3);
				put(hTable);
			}
			else if(eventMod10(2)) {
				Thread.sleep(11);
				throw new TimeoutException();
			}
			else if(eventMod10(3)) {
				put(hTable);
				pool.checkIn(hTable, false);//will result in a double checkIn
			}else {
				put(hTable);
			}
			return null;
		}

		boolean eventMod10(int... matches) {
			int mod = (int)(randomLong % 10);
			return ArrayTool.containsUnsorted(matches, mod);
		}

		void put(HTable hTable) throws InterruptedException, IOException {
			List<Row> actions = ListTool.createArrayList();
			Put put = new Put(LongByteTool.getComparableBytes(randomLong));
			Field<?> dummyField = new ByteField(HBaseNode.DUMMY, (byte)0);
			put.add(HBaseNode.FAM, dummyField.getColumnNameBytes(), dummyField.getBytes());
			actions.add(put);
			put.setWriteToWAL(false);
			hTable.batch(actions);
		}
	}
}







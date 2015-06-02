package com.hotpads.datarouter.client.imp.hbase.pool;

import java.io.IOException;
import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.pool.PoolTestBean;
import com.hotpads.datarouter.test.client.pool.PoolTestBeanKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.datastructs.MutableString;

public class HTableExecutorServicePoolTester {
	private static Logger logger = LoggerFactory.getLogger(HTableExecutorServicePoolTester.class);

	static final int NUM_INSERTS = 200000;
	static final int TIMEOUT_MS = 10;
	
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

	public static void main(String... args) throws IOException{
		init();
		ThreadPoolExecutor exec = new ThreadPoolExecutor(30, 30,
				60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
				new ThreadPoolExecutor.CallerRunsPolicy());

		int npes=0, toes=0;

		Random random = new Random();
		List<ActionUsingPool> tasks = new ArrayList<>();
		List<Future<Void>> futures = new ArrayList<>();
		for(int i=0; i < NUM_INSERTS; ++i) {
			long randomLong = RandomTool.nextPositiveLong(random);
			ActionUsingPool task = new ActionUsingPool(randomLong);
			tasks.add(task);
			futures.add(exec.submit(task));
		}
		for(int i=0; i < NUM_INSERTS; ++i) {
			Future<Void> future = futures.get(i);
			try{
//				future.get();
				future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
			} catch(TimeoutException e){
//				logger.warn("progress="+tasks.get(i).progress);
//				logger.error(ExceptionTool.getStackTraceAsString(e));
			} catch(RuntimeException e){
				logger.error("", e);
			} catch(ExecutionException e){
				if(e.getCause() instanceof DataAccessException) {
					DataAccessException purposefulException = (DataAccessException)e.getCause();
					if(purposefulException.getCause() instanceof TimeoutException) {
						++toes;
					}else if(purposefulException.getCause() instanceof NullPointerException) {
						++npes;
					}
				}else {
					logger.error("", e);
				}
			} catch(InterruptedException e){
				logger.error("", e);
			}

			if(i % 10000 == 0) {
				logger.warn("did "+i+", NPEs:"+npes+", TOEs:"+toes);
				
			}
		}
		exec.shutdownNow();
	}

	
	/********************* inner class ****************************/
	
	static class ActionUsingPool implements Callable<Void>{
		MutableString progress;
		long randomLong;
		public ActionUsingPool(long randomLong){
			this.randomLong = randomLong;
			this.progress = new MutableString("constructing");
		}


		@Override
		public Void call(){
			HTable hTable = null;
			boolean possiblyTarnishedHTable = false;
			try{
				hTable = client.checkOutHTable(tableName, null);
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
			}
			else if(eventMod10(4)) {
				int sleepForMs = 5*TIMEOUT_MS;
				progress.set("about to sleep for "+sleepForMs);
				Thread.sleep(sleepForMs);
			}
			else {
				put(hTable);
			}
			return null;
		}

		boolean eventMod10(int... matches) {
			int mod = (int)(randomLong % 10);
			return DrArrayTool.containsUnsorted(matches, mod);
		}

		void put(HTable hTable) throws InterruptedException, IOException {
			List<Row> actions = new ArrayList<>();
			Put put = new Put(LongByteTool.getComparableBytes(randomLong));
			Field<?> dummyField = new SignedByteField(HBaseNode.DUMMY, (byte)0);
			put.add(HBaseNode.FAM, dummyField.getKey().getColumnNameBytes(), dummyField.getBytes());
			actions.add(put);
			put.setWriteToWAL(false);
			hTable.batch(actions);
		}
	}
}








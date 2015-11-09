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

import javax.inject.Inject;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hbase.test.pool.BasicClientTestRouter;
import com.hotpads.datarouter.client.imp.hbase.test.pool.PoolTestBean;
import com.hotpads.datarouter.client.imp.hbase.test.pool.PoolTestBeanKey;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.datastructs.MutableString;

//TODO this won't work yet because of a runtime JDBC dependency in BasicClientTestRouter
@Guice(moduleFactory = DatarouterTestModuleFactory.class)
public class HTableExecutorServicePoolTester{
	private static final Logger logger = LoggerFactory.getLogger(HTableExecutorServicePoolTester.class);

	private static final int NUM_INSERTS = 200000;
	private static final int TIMEOUT_MS = 10;
	private static final String TABLE_NAME = PoolTestBean.class.getSimpleName();

	@Inject
	private BasicClientTestRouter router;

	private HBaseClientImp client;
	private HTableExecutorServicePool pool;
	private MapStorage<PoolTestBeanKey,PoolTestBean> node;


	@BeforeClass
	public void beforeClass(){
		client = (HBaseClientImp)router.getClient(DrTestConstants.CLIENT_drTestHBase.getName());
		//yes, this test will fail if we change the pool type
		pool = (HTableExecutorServicePool)client.getHTablePool();
		node = router.poolTestBeanHBase();
		warmUp();
	}

	private void warmUp(){
//		node.deleteAll(null);
//		Assert.assertEquals(0, CollectionTool.size(node.getAll(null)));
		node.put(new PoolTestBean(12345L), null);
	}


	@Test
	public void bigTest(){
		ThreadPoolExecutor exec = new ThreadPoolExecutor(30, 30,
				60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
				new ThreadPoolExecutor.CallerRunsPolicy());

		int numNpes = 0;
		int numTimeouts = 0;

		Random random = new Random();
		List<ActionUsingPool> tasks = new ArrayList<>();
		List<Future<Void>> futures = new ArrayList<>();
		for(int i=0; i < NUM_INSERTS; ++i) {
			long randomLong = RandomTool.nextPositiveLong(random);
			ActionUsingPool task = new ActionUsingPool(client, pool, randomLong);
			tasks.add(task);
			futures.add(exec.submit(task));
		}
		for(int i=0; i < NUM_INSERTS; ++i){
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
				if(e.getCause() instanceof DataAccessException){
					DataAccessException purposefulException = (DataAccessException)e.getCause();
					if(purposefulException.getCause() instanceof TimeoutException) {
						++numTimeouts;
					}else if(purposefulException.getCause() instanceof NullPointerException){
						++numNpes;
					}
				}else {
					logger.error("", e);
				}
			} catch(InterruptedException e){
				logger.error("", e);
			}

			if(i % 10000 == 0) {
				logger.warn("did {}, NPEs:{}, TOEs:{}", i, numNpes, numTimeouts);

			}
		}
		exec.shutdownNow();
	}


	/********************* inner class ****************************/

	private static class ActionUsingPool implements Callable<Void>{
		private final HBaseClientImp client;
		private final HTableExecutorServicePool pool;
		private final MutableString progress;

		private long randomLong;

		public ActionUsingPool(HBaseClientImp client, HTableExecutorServicePool pool, long randomLong){
			this.client = client;
			this.pool = pool;
			this.progress = new MutableString("constructing");
			this.randomLong = randomLong;
		}


		@Override
		public Void call(){
			Table table = null;
			boolean possiblyTarnishedHTable = false;
			try{
				table = client.checkOutTable(TABLE_NAME, null);
				return hbaseCall(table);
			}catch(Exception e){
				possiblyTarnishedHTable = true;
				throw new DataAccessException(e);
			}finally{
				if(table==null){
					logger.warn("not checking in HTable because it's null");
				}else if(client==null){
					logger.warn("not checking in HTable because client is null");
				}else{
					client.checkInTable(table, possiblyTarnishedHTable);
				}
				pool.forceLogIfInconsistentCounts(false, TABLE_NAME);
			}
		}


		private Void hbaseCall(Table table) throws TimeoutException, InterruptedException, IOException{
			if(eventMod10(0)) {
				throw new NullPointerException();
			}else if(eventMod10(1)) {
				Thread.sleep(3);
				put(table);
			}else if(eventMod10(2)) {
				Thread.sleep(11);
				throw new TimeoutException();
			}else if(eventMod10(3)) {
				put(table);
				pool.checkIn(table, false);//will result in a double checkIn
			}else if(eventMod10(4)) {
				int sleepForMs = 5*TIMEOUT_MS;
				progress.set("about to sleep for "+sleepForMs);
				Thread.sleep(sleepForMs);
			}else {
				put(table);
			}
			return null;
		}


		private boolean eventMod10(int... matches) {
			int mod = (int)(randomLong % 10);
			return DrArrayTool.containsUnsorted(matches, mod);
		}


		private void put(Table table) throws InterruptedException, IOException {
			List<Row> actions = new ArrayList<>();
			Put put = new Put(LongByteTool.getComparableBytes(randomLong));
			Field<?> dummyField = new SignedByteField(HBaseNode.DUMMY, (byte)0);
			put.add(HBaseNode.FAM, dummyField.getKey().getColumnNameBytes(), dummyField.getBytes());
			actions.add(put);
			put.setWriteToWAL(false);
			table.batch(actions);
		}
	}
}








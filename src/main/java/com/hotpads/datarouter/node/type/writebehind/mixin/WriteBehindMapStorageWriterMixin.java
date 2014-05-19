package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.OutstandingWriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public class WriteBehindMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
implements MapStorageWriter<PK,D>{
	private Logger logger = Logger.getLogger(getClass());
	
	protected BaseWriteBehindNode<PK,D,N> node;
	private ScheduledExecutorService flushScheduler;

	private BlockingQueue<PutWrapper<PK, D>> queue;
	
	public WriteBehindMapStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node){
		this.node = node;
		this.queue = new LinkedBlockingDeque<PutWrapper<PK, D>>();
		this.flushScheduler = Executors.newScheduledThreadPool(1);
		this.flushScheduler.scheduleWithFixedDelay(new Flusher(), 500, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	public void delete(final PK key, final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().delete(key, config);
				}catch(Exception e){
					logger.error("error on delete["+key.toString()+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void deleteAll(final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().deleteAll(config);
				}catch(Exception e){
					logger.error("error on deleteAll");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){
			public Void call(){
				try{
					node.getBackingNode().deleteMulti(keys, config);
				}catch(Exception e){
					logger.error("error on deleteMulti including["+CollectionTool.getFirst(keys)+"]");
					logger.error(ExceptionTool.getStackTraceAsString(e));
				}
				return null; 
			}
		})));
	}

	@Override
	public void put(D databean, Config config) {
		queue.offer(new PutWrapper<PK, D>(ListTool.wrap(databean), config));
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		queue.offer(new PutWrapper<PK, D>(databeans, config));
	}

	private class Flusher implements Runnable{
		private static final int FLUSH_BATCH_SIZE = 100;

		@Override
		public void run(){
			final List<D> flushBatch = ListTool.createArrayList();
			PutWrapper<PK, D> putWrapper;
			while(CollectionTool.notEmpty(queue)){
				putWrapper = queue.poll();
				if (putWrapper.getConfig() == null) {
					flushBatch.addAll(putWrapper.getDatabeans());//FIXME can excedd batch size
				} else {
					writeMulti(putWrapper.getDatabeans(), putWrapper.getConfig());
				}
				if (flushBatch.size() >= FLUSH_BATCH_SIZE) {
					writeMulti(flushBatch, null);
					flushBatch.clear();
				}
			}
			if (!CollectionTool.isEmpty(flushBatch)) {
				writeMulti(flushBatch, null);//don't forget the last not full batch
			}
		}

	}

	private void writeMulti(final Collection<D> flushBatch, final Config config) {
		node.getOutstandingWrites().add(new OutstandingWriteWrapper(
				System.currentTimeMillis(), 
				node.getWriteExecutor().submit(new Callable<Void>(){

					public Void call(){
						try{
							node.getBackingNode().putMulti(flushBatch, config);
						}catch(Exception e){
							logger.error("error on putMulti");
							logger.error(ExceptionTool.getStackTraceAsString(e));
						}
						return null; 
					}

				})));
	}
}
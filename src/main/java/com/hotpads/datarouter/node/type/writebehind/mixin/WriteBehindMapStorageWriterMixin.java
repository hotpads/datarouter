package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.type.writebehind.base.BaseWriteBehindNode;
import com.hotpads.datarouter.node.type.writebehind.base.OutstandingWriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class WriteBehindMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
implements MapStorageWriter<PK,D>{
	private Logger logger = Logger.getLogger(getClass());

	private static final int FLUSH_BATCH_SIZE = 100;

	protected BaseWriteBehindNode<PK,D,N> node;

	private BlockingQueue<PutWrapper<PK, D>> putQueue;
	private BlockingQueue<DeleteWrapper<PK>> deleteQueue;
	
	public WriteBehindMapStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node){
		this.node = node;
		this.putQueue = new LinkedBlockingDeque<PutWrapper<PK, D>>();
		this.deleteQueue = new LinkedBlockingDeque<DeleteWrapper<PK>>();
		this.node.getFlushScheduler().scheduleWithFixedDelay(new PutFlusher(), 500, 500, TimeUnit.MILLISECONDS);
		this.node.getFlushScheduler().scheduleWithFixedDelay(new DeleteFlusher(), 500, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	public void delete(final PK key, final Config config){
		deleteQueue.offer(new DeleteWrapper<PK>(ListTool.wrap(key), config));
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		deleteQueue.offer(new DeleteWrapper<PK>(keys, config));
	}

	@Override
	public void deleteAll(final Config config){
		node.getOutstandingWrites().add(
				new OutstandingWriteWrapper(System.currentTimeMillis(), node.getWriteExecutor().submit(
						new Callable<Void>(){
							public Void call(){
								try{
									node.getBackingNode().deleteAll(config);
								}catch(Exception e){
									logger.error("error on deleteAll", e);
								}
								return null;
							}
						})));
	}

	@Override
	public void put(D databean, Config config) {
		putQueue.offer(new PutWrapper<PK, D>(ListTool.wrap(databean), config));
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		putQueue.offer(new PutWrapper<PK, D>(databeans, config));
	}

	public class PutFlusher implements Runnable{

		@Override
		public void run(){
			final List<D> flushBatch = ListTool.createArrayList();
			while(CollectionTool.notEmpty(putQueue)){
				PutWrapper<PK,D> putWrapper = putQueue.poll();
				if (putWrapper.getConfig() == null) {
					List<List<D>> batches = BatchTool.getBatches(putWrapper.getDatabeans(), FLUSH_BATCH_SIZE);
					for(List<D> batche : batches){
						flushBatch.addAll(batche);
						if (flushBatch.size() == FLUSH_BATCH_SIZE) {
							writeMulti(flushBatch, null);
							flushBatch.clear();
						}
					}
				} else {
					writeMulti(putWrapper.getDatabeans(), putWrapper.getConfig());
				}
			}
			if (!CollectionTool.isEmpty(flushBatch)) {
				writeMulti(flushBatch, null);//don't forget the last not full batch
			}
		}

	}
	
	public class DeleteFlusher implements Runnable{

		@Override
		public void run(){
			final List<PK> flushBatch = ListTool.createArrayList();
			while(CollectionTool.notEmpty(deleteQueue)){
				DeleteWrapper<PK> deleteWrapper = deleteQueue.poll();
				if (deleteWrapper.getConfig() == null) {
					List<List<PK>> batches = BatchTool.getBatches(deleteWrapper.getKeys(), FLUSH_BATCH_SIZE);
					for(List<PK> batche : batches){
						flushBatch.addAll(batche);
						if (flushBatch.size() == FLUSH_BATCH_SIZE) {
							removeMulti(flushBatch, null);
							flushBatch.clear();
						}
					}
				} else {
					removeMulti(deleteWrapper.getKeys(), deleteWrapper.getConfig());
				}
			}
			if (!CollectionTool.isEmpty(flushBatch)) {
				removeMulti(flushBatch, null);//don't forget the last not full batch
			}
		}

	}

	public void writeMulti(final Collection<D> flushBatch, final Config config){
		node.getOutstandingWrites().add(
				new OutstandingWriteWrapper(System.currentTimeMillis(), node.getWriteExecutor().submit(
						new Callable<Void>(){

							public Void call(){
								try{
									node.getBackingNode().putMulti(flushBatch, config);
								}catch(Exception e){
									logger.error("error on putMulti", e);
								}
								return null;
							}

						})));
	}

	private void removeMulti(final Collection<PK> keys, final Config config){
		node.getOutstandingWrites().add(
				new OutstandingWriteWrapper(System.currentTimeMillis(), node.getWriteExecutor().submit(
						new Callable<Void>(){
							public Void call(){
								try{
									node.getBackingNode().deleteMulti(keys, config);
								}catch(Exception e){
									logger.error("error on deleteMulti including[" + CollectionTool.getFirst(keys)
											+ "]", e);
								}
								return null;
							}
						})));
	}

}
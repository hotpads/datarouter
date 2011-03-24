package com.hotpads.datarouter.backup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.profile.PhaseTimer;

public abstract class RestoreRegion<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
	protected static Logger logger = Logger.getLogger(RestoreRegion.class);
	
	protected DataRouter router;
	protected Class<D> cls;
	protected MapStorageNode<PK,D> node;
	protected Integer putBatchSize;
	protected Boolean ignoreNullFields;
	protected Map<String,Field<?>> fieldByPrefixedName;
	
	protected InputStream is;

	protected Long rawBytes = 0L;
	protected Long compressedBytes = 0L;
	protected Long numRecords = 0L;
	
	protected Integer logEvery;
	
	public RestoreRegion(Class<D> cls, DataRouter router, MapStorageNode<PK,D> node, 
			Integer putBatchSize, Boolean ignoreNullFields){
		this.cls = cls;
		this.router = router;
		this.node = node;
		this.putBatchSize = putBatchSize;
		this.ignoreNullFields = ignoreNullFields;
		this.logEvery = 10 * putBatchSize;
		this.fieldByPrefixedName = MapTool.createHashMap();
		for(Field<?> field : IterableTool.nullSafe(node.getFields())){
			this.fieldByPrefixedName.put(field.getPrefixedName(), field);
		}
	}
	
	public abstract void execute();
	
	protected void importAndCloseInputStream(){
		try{
			List<D> toSave = ListTool.createLinkedList();
			PhaseTimer putBatchTimer = new PhaseTimer();
			PhaseTimer logBatchTimer = new PhaseTimer();
			while(true){
				try{
					D databean = FieldSetTool.fieldSetFromByteStream(cls, fieldByPrefixedName, is);
					toSave.add(databean);
					++numRecords;
					if(numRecords % logEvery == 0){
						logBatchTimer.add("imported "+logEvery+" rows");
						logger.warn("imported "+NumberFormatter.addCommas(numRecords)+" from "+toSave.get(0).getKey()
								+", batchSize:"+putBatchSize+", rps:"+logBatchTimer.getItemsPerSecond(logEvery));
						logBatchTimer = new PhaseTimer();
					}
					if(toSave.size() >= putBatchSize){
						putBatchTimer.add("parsed "+toSave.size());
						node.putMulti(toSave, new Config().setNumAttempts(20).setTimeout(30, TimeUnit.SECONDS)
								.setPersistentPut(false).setIgnoreNullFields(ignoreNullFields));
						putBatchTimer.add("saved "+toSave.size());
//						logger.warn(timer);
						putBatchTimer = new PhaseTimer();
						toSave.clear();
					}
				}catch(IllegalArgumentException iac){
					if(toSave.size() >= 0){//don't forget these
						node.putMulti(toSave, new Config().setNumAttempts(20).setTimeout(30, TimeUnit.SECONDS)
								.setPersistentPut(false).setIgnoreNullFields(ignoreNullFields));
						logger.warn("imported "+NumberFormatter.addCommas(numRecords)+" from "+toSave.get(0).getKey());
					}
					break;//VarLong throws this at the end of the InputStream
				}
			}
		}catch(IOException ioe){
			throw new RuntimeException(ioe);
		}finally{
			try{
				if(is!=null){ is.close(); }
			}catch(IOException ioe){
				throw new RuntimeException(ioe);
			}
		}
	}

	public Long getRawBytes(){
		return rawBytes;
	}

	public Long getCompressedBytes(){
		return compressedBytes;
	}

	public Long getNumRecords(){
		return numRecords;
	}
	
	
	
}

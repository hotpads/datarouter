package com.hotpads.datarouter.backup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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

public abstract class RestoreRegion<PK extends PrimaryKey<PK>,D extends Databean<PK>>{
	protected static Logger logger = Logger.getLogger(RestoreRegion.class);
	
	protected DataRouter router;
	protected Class<D> cls;
	protected MapStorageNode<PK,D> node;
	protected Map<String,Field<?>> fieldByPrefixedName;
	
	protected InputStream is;

	protected Long rawBytes = 0L;
	protected Long compressedBytes = 0L;
	protected Long numRecords = 0L;
	
	public RestoreRegion(Class<D> cls, 
			DataRouter router, MapStorageNode<PK,D> node){
		this.cls = cls;
		this.router = router;
		this.node = node;
		this.fieldByPrefixedName = MapTool.createHashMap();
		for(Field<?> field : IterableTool.nullSafe(node.getFields())){
			this.fieldByPrefixedName.put(field.getPrefixedName(), field);
		}
	}
	
	public abstract void execute();
	
	public static final int PUT_BATCH_SIZE = 100;
	
	protected void importAndCloseInputStream(){
		try{
			List<D> toSave = ListTool.createLinkedList();
			PhaseTimer timer = new PhaseTimer();
			while(true){
				try{
//					int databeanLength = (int)new VarLong(is).getValue();
//					byte[] databeanBytes = new byte[databeanLength];
//					is.read(databeanBytes);
//					ByteArrayInputStream databeanInputStream = new ByteArrayInputStream(databeanBytes);
					D databean = FieldSetTool.fieldSetFromBytes(cls, fieldByPrefixedName, is);
					toSave.add(databean);
					++numRecords;
					if(numRecords % 10000 == 0){
						logger.warn("imported "+NumberFormatter.addCommas(numRecords)+" from "+toSave.get(0).getKey());
					}
					if(toSave.size() >= 100){
						timer.add("parsed "+toSave.size());
						node.putMulti(toSave, null);
						timer.add("saved "+toSave.size());
//						logger.warn(timer);
						timer = new PhaseTimer();
						toSave.clear();
					}
				}catch(IllegalArgumentException iac){
					if(toSave.size() >= 0){//don't forget these
						node.putMulti(toSave, null);
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

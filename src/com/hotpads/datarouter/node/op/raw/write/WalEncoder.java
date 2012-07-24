package com.hotpads.datarouter.node.op.raw.write;

import java.util.Collection;

import com.amazonaws.services.sqs.model.Message;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.wal.WalMessage;
import com.hotpads.util.wal.WriteAheadLog;

public class WalEncoder<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> implements MapStorageWriter<PK ,D >{

	/************************** fields *****************************/
	protected WriteAheadLog wal;
	private Collection databeans;
	
	
	/************************** constructors *****************************/
	
	public WalEncoder(WriteAheadLog wal){
		this.wal = wal;
	}

	
	/************************** methods *****************************/
	
	@Override
	public void put(D databean, Config config){
		Collection<Field<?>> fields = databean.getFields();
		boolean includePrefix = true;
		byte[] serializedDataBean = FieldSetTool.getSerializedKeyValues(fields, includePrefix);
		wal.append(new WalMessage(new ByteRange(serializedDataBean)));
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		this.databeans = databeans;
		for(D databean : databeans){
			put(databean, config);
		}
	
	}

	@Override
	public void delete(PrimaryKey key, Config config){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMulti(Collection keys, Config config){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(Config config){
		// TODO Auto-generated method stub
		
	}

}

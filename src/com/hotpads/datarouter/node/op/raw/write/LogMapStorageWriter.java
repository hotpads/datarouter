package com.hotpads.datarouter.node.op.raw.write;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.wal.WriteAheadLog;

public class LogMapStorageWriter<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> implements MapStorageWriter<PK,D>{

	protected WriteAheadLog wal;
	
	public LogMapStorageWriter(WriteAheadLog wal){
		this.wal = wal;
	}

	@Override
	public void put(D databean, Config config){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(PK key, Config config){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(Config config){
		// TODO Auto-generated method stub
		
	}

}

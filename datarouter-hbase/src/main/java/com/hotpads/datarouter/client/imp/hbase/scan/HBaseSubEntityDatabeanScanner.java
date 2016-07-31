package com.hotpads.datarouter.client.imp.hbase.scan;

import org.apache.hadoop.hbase.KeyValue;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Range;

public class HBaseSubEntityDatabeanScanner<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseHBaseSubEntityScanner<EK,E,PK,D,F,D>{

	private final DatabeanFieldInfo<PK,D,F> fieldInfo;
	private D nextDatabean;

	public HBaseSubEntityDatabeanScanner(DatabeanFieldInfo<PK,D,F> fieldInfo,
			HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser, HBaseSubEntityKvScanner<EK,E,PK,D,F> kvScanner,
			Range<PK> range){
		super(resultParser, kvScanner, range);
		this.fieldInfo = fieldInfo;
	}


	//HBaseSubEntityKvScanner may give us extra cells, so do some extra filtering here
	@Override
	public boolean advance(){
		if(finished){
			return false;
		}
		while(kvScanner.advance()){
			KeyValue kv = kvScanner.getCurrent();
			Pair<PK,String> pkAndFieldName = resultParser.parsePrimaryKeyAndFieldName(kv);
			PK pk = pkAndFieldName.getLeft();
			if(shouldSkip(pk)){
				continue;
			}
			if(passedEndKey(pk)){
				current = null;
				finished = true;
				return false;
			}

			boolean swapCurrent = nextDatabean == null || DrObjectTool.notEquals(nextDatabean.getKey(), pk);
			if(swapCurrent){
				current = nextDatabean;
				nextDatabean = resultParser.makeDatabeanWithOneField(kv);
				return true;
			}

			resultParser.setDatabeanField(nextDatabean, pkAndFieldName.getRight(), kv.getValue());
		}
		return false;
	}

}

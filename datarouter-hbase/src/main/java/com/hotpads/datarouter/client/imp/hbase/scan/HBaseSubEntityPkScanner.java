package com.hotpads.datarouter.client.imp.hbase.scan;

import java.util.Objects;

import org.apache.hadoop.hbase.KeyValue;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Range;

public class HBaseSubEntityPkScanner<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseHBaseSubEntityScanner<EK,E,PK,D,F,PK>{

	public HBaseSubEntityPkScanner(HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser,
			HBaseSubEntityKvScanner<EK,E,PK,D,F> kvScanner, Range<PK> range){
		super(resultParser, kvScanner, range);
	}


	//HBaseSubEntityKvScanner may give us extra cells, so do some extra filtering here
	@Override
	public boolean advance(){
		if(finished){
			return false;
		}
		while(kvScanner.advance()){
			KeyValue kv = kvScanner.getCurrent();
			//TODO could avoid building a new PK for each cell (doing byte[] comparisons instead)
			Pair<PK,String> pkAndFieldName = resultParser.parsePrimaryKeyAndFieldName(kv);
			PK pk = pkAndFieldName.getLeft();
			if(isBeforeStartOfRange(pk)){
				continue;
			}
			if(Objects.equals(current, pk)){
				continue;//don't replace the current one which would break the == operator
			}
			if(isAfterEndOfRange(pk)){
				current = null;
				finished = true;
				return false;
			}
			current = pk;
			return true;
		}
		return false;
	}

}

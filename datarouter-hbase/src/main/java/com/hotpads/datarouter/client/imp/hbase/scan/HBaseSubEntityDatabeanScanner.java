package com.hotpads.datarouter.client.imp.hbase.scan;

import org.apache.hadoop.hbase.KeyValue;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
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

	private D nextDatabean;

	public HBaseSubEntityDatabeanScanner(HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser,
			HBaseSubEntityKvScanner<EK,E,PK,D,F> kvScanner, Range<PK> range){
		super(resultParser, kvScanner, range);
	}


	//HBaseSubEntityKvScanner may give us extra cells, so do some extra filtering here
	@Override
	public boolean advance(){
		if(finished){
			current = null;
			return false;
		}
		while(kvScanner.advance()){
			KeyValue kv = kvScanner.getCurrent();
			Pair<PK,String> pkAndFieldName = resultParser.parsePrimaryKeyAndFieldName(kv);
			PK pk = pkAndFieldName.getLeft();
			if(isBeforeStartOfRange(pk)){
				continue;
			}
			if(nextDatabean == null){//init first databean
				promoteNextToCurrentAndSetNextTo(resultParser.makeDatabeanWithOneField(kv));
				continue;
			}
			if(DrObjectTool.notEquals(nextDatabean.getKey(), pk)){
				promoteNextToCurrentAndSetNextTo(resultParser.makeDatabeanWithOneField(kv));
				if(isAfterEndOfRange(pk)){
					finished = true;
				}
				return true;
			}

			resultParser.setDatabeanField(nextDatabean, pkAndFieldName.getRight(), kv.getValue());
		}
		if(nextDatabean != null){
			promoteNextToCurrentAndSetNextTo(null);
			finished = true;
			return true;
		}
		return false;
	}


	private void promoteNextToCurrentAndSetNextTo(D next){
		current = nextDatabean;
		System.out.println("updated to " + current);
		nextDatabean = next;
	}

}

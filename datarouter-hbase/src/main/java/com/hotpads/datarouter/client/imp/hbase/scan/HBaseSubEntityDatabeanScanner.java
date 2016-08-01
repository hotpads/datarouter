package com.hotpads.datarouter.client.imp.hbase.scan;

import org.apache.hadoop.hbase.Cell;

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
			HBaseSubEntityCellScanner<EK,E,PK,D,F> cellScanner, Range<PK> range){
		super(resultParser, cellScanner, range);
	}


	//HBaseSubEntityKvScanner may give us extra cells, so do some extra filtering here
	@Override
	public boolean advance(){
		if(finished){
			current = null;
			return false;
		}
		while(cellScanner.advance()){
			Cell cell = cellScanner.getCurrent();
			Pair<PK,String> pkAndFieldName = resultParser.parsePrimaryKeyAndFieldName(cell);
			PK pk = pkAndFieldName.getLeft();
			if(isBeforeStartOfRange(pk)){
				continue;
			}
			if(nextDatabean == null){//init first databean
				promoteNextToCurrentAndSetNextTo(resultParser.makeDatabeanWithOneField(cell));
				continue;
			}
			if(DrObjectTool.notEquals(nextDatabean.getKey(), pk)){
				promoteNextToCurrentAndSetNextTo(resultParser.makeDatabeanWithOneField(cell));
				if(isAfterEndOfRange(pk)){//the kv scanner returned more kvs than we wanted
					finished = true;
				}
				return true;
			}

			resultParser.setDatabeanField(nextDatabean, pkAndFieldName.getRight(), cell.getValue());
		}
		if(nextDatabean != null){//put the last databean in place
			promoteNextToCurrentAndSetNextTo(null);
			finished = true;
			return true;
		}
		return false;
	}


	private void promoteNextToCurrentAndSetNextTo(D next){
		current = nextDatabean;
		nextDatabean = next;
	}

}

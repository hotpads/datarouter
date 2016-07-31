package com.hotpads.datarouter.client.imp.hbase.scan;

import java.util.Objects;

import org.apache.hadoop.hbase.KeyValue;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.compare.FieldSetRangeFilter;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;

public class HBaseSubEntityPrimaryKeyScanner<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseSortedScanner<PK>{

	private final HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser;
	private final HBaseSubEntityKvScanner<EK,E,PK,D,F> kvScanner;
	private final Range<PK> range;

	private PK currentPk;
	private boolean finished = false;

	public HBaseSubEntityPrimaryKeyScanner(HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser,
			HBaseSubEntityKvScanner<EK,E,PK,D,F> kvScanner, Range<PK> range){
		this.resultParser = resultParser;
		this.kvScanner = kvScanner;
		this.range = range;
	}

	@Override
	public PK getCurrent(){
		return currentPk;
	}

	//HBaseSubEntityKvScanner may give us extra cells, so do some extra filtering here
	@Override
	public boolean advance(){
		if(finished){
			return false;
		}
		while(kvScanner.advance()){
			KeyValue kv = kvScanner.getCurrent();
			//TODO could avoid building a new PK for each cell in the Databean
			Pair<PK,String> pkAndFieldName = resultParser.parsePrimaryKeyAndFieldName(kv);
			PK pk = pkAndFieldName.getLeft();
			if( ! range.matchesStart(pk)){
				continue;
			}
			if(Objects.equals(currentPk, pk)){
				continue;//don't replace the current one which would break the == operator
			}
			if(passedEndKey(pk)){
				currentPk = null;
				finished = true;
				return false;
			}
			currentPk = pk;
			return true;
		}
		return false;
	}


	private boolean passedEndKey(PK pk){
		if( ! range.hasEnd()){
			return false;
		}
		return ! FieldSetRangeFilter.isCandidateBeforeEndOfRange(pk.getFields(), range.getEnd().getFields(), range
				.getEndInclusive());
	}
}

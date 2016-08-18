package com.hotpads.datarouter.client.imp.hbase.scan;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.compare.FieldSetRangeFilter;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;

public abstract class BaseHBaseSubEntityScanner<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		T extends Comparable<? super T>>
extends BaseSortedScanner<T>{

	protected final HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser;
	protected final HBaseSubEntityCellScanner<EK,E,PK,D,F> cellScanner;
	private final Range<PK> range;

	protected T current;
	protected boolean finished;

	public BaseHBaseSubEntityScanner(HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser,
			HBaseSubEntityCellScanner<EK,E,PK,D,F> cellScanner, Range<PK> range){
		this.resultParser = resultParser;
		this.cellScanner = cellScanner;
		this.range = range;
		this.finished = false;
	}

	@Override
	public T getCurrent(){
		return current;
	}

	protected boolean isBeforeStartOfRange(PK pk){
		return range != null && ! range.matchesStart(pk);
	}

	protected boolean isAfterEndOfRange(PK pk){
		if(range == null || ! range.hasEnd()){
			return false;
		}
		return ! FieldSetRangeFilter.isCandidateBeforeEndOfRange(pk.getFields(), range.getEnd().getFields(), range
				.getEndInclusive());
	}
}

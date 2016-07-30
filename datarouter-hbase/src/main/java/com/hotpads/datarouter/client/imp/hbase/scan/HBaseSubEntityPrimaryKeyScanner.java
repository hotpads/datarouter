package com.hotpads.datarouter.client.imp.hbase.scan;

import org.apache.hadoop.hbase.KeyValue;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode.HBaseSubEntityResultScanner;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityResultParser;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;

public class HBaseSubEntityPrimaryKeyScanner<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseSortedScanner<PK>{

	private final HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser;
	private final HBaseSubEntityResultScanner kvScanner;

	private PK current;

	public HBaseSubEntityPrimaryKeyScanner(HBaseSubEntityResultParser<EK,E,PK,D,F> resultParser,
			HBaseSubEntityResultScanner kvScanner){
		this.resultParser = resultParser;
		this.kvScanner = kvScanner;
	}

	@Override
	public PK getCurrent(){
		return current;
	}

	@Override
	public boolean advance(){
		while(kvScanner.advance()){
			KeyValue kv = kvScanner.getCurrent();
			//TODO could avoid building a new PK for each cell in the Databean
			Pair<PK,String> pkAndFieldName = resultParser.parsePrimaryKeyAndFieldName(kv);
			PK pk = pkAndFieldName.getLeft();
			if(DrObjectTool.notEquals(current, pk)){
				current = pk;
				return true;
			}
		}
		return false;
	}

}

package com.hotpads.datarouter.client.imp.hbase.op.write;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Row;

import com.hotpads.datarouter.client.ClientTableNodeNames;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityQueryBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.entity.EntityTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.bytes.StringByteTool;

public class HBaseSubEntityIncrementOp<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends HBaseTask<Void>{

	public static final String OP_increment = "increment";
	
	private final HBaseSubEntityNode<EK, E, PK, D, F> node;
	private final Map<PK,Map<String,Long>> countByColumnByKey;
	private final Config config;
	private final HBaseSubEntityQueryBuilder<EK,E,PK,D,F> queryBuilder;
	
	public HBaseSubEntityIncrementOp(HBaseSubEntityNode<EK, E, PK, D, F> node, Map<PK,Map<String,Long>> 
		countByColumnByKey, Config config, HBaseSubEntityQueryBuilder<EK, E, PK, D, F> queryBuilder){
		super(node.getDatarouterContext(), new ClientTableNodeNames(node.getClientId().getName(), node.getTableName(),
				node.getName()), "HBaseTask." + OP_increment, config);
		this.node = node;
		this.countByColumnByKey = countByColumnByKey;
		this.config = Config.nullSafe(config);
		this.queryBuilder = queryBuilder;
	}

	@Override
	public Void hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception{
		List<Row> actions = new ArrayList<>();
		int numCellsIncremented = 0, numRowsIncremented = 0;
		Map<EK,List<PK>> keysByEntityKey = EntityTool.getPrimaryKeysByEntityKey(countByColumnByKey.keySet());
		for(EK ek : keysByEntityKey.keySet()){ //TODO: respect config commitbatch size
			byte[] ekBytes = queryBuilder.getRowBytesWithPartition(ek);
			Increment increment = new Increment(ekBytes);
			for(PK key : keysByEntityKey.get(ek)){
				byte[] qualifierPkBytes = queryBuilder.getQualifierPkBytes(key, true);
				for(Entry<String, Long> entry : countByColumnByKey.get(key).entrySet()) {
					assertColumnIsUInt63Field(entry.getKey());
					byte[] fullQualifierBytes = DrByteTool.concatenate(node.getFieldInfo().getEntityColumnPrefixBytes(),
							qualifierPkBytes, StringByteTool.getUtf8Bytes(entry.getKey()));
					increment.addColumn(HBaseSubEntityNode.FAM, fullQualifierBytes, entry.getValue());
					++numCellsIncremented;
				}
				++numRowsIncremented;
			}
			increment.setWriteToWAL(config.getPersistentPut());
			actions.add(increment);
		}
		if (DrCollectionTool.notEmpty(actions)){
			hTable.batch(actions);
			hTable.flushCommits();
		}
		DRCounters.incClientNodeCustom(client.getType(), "cells incremented", node.getClientId().getName(),
				node.getName(), numCellsIncremented);
		DRCounters.incClientNodeCustom(client.getType(), "rows incremented", node.getClientId().getName(),
				node.getName(), numRowsIncremented);
		return null;
	}
	
	//try to prevent making a mistake with columnName and incrementing a non-counter column
	private void assertColumnIsUInt63Field(String columnName){
		Class<? extends Field> columnType = node.getFieldInfo().getFieldTypeForColumn(columnName);
		if(DrObjectTool.notEquals(columnType, UInt63Field.class)){
			throw new IllegalArgumentException(columnName+" is a "+columnType.getClass()
					+", but you can only increment a UInt63Field");
		}
	}

}

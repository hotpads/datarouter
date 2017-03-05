package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public class HBaseEntityResultParser<EK extends EntityKey<EK>,E extends Entity<EK>>{

	private final EntityFieldInfo<EK,E> entityFieldInfo;
	private final Map<String,HBaseSubEntityReaderNode<EK,E,?,?,?>> nodeByQualifierPrefix;

	public HBaseEntityResultParser(EntityFieldInfo<EK,E> entityFieldInfo,
			Map<String,HBaseSubEntityReaderNode<EK,E,?,?,?>> nodeByQualifierPrefix){
		this.entityFieldInfo = entityFieldInfo;
		this.nodeByQualifierPrefix = nodeByQualifierPrefix;
	}

	public E parseEntity(EK ek, Result row){
		if(row == null){
			return null;
		}
		Class<E> entityClass = entityFieldInfo.getEntityClass();
		E entity = ReflectionTool.create(entityClass);
		entity.setKey(ek);
		Map<String,List<? extends Databean<?,?>>> databeansByQualifierPrefix = getDatabeansByQualifierPrefix(row);
		for(String qualifierPrefix : DrMapTool.nullSafe(databeansByQualifierPrefix).keySet()){
			HBaseSubEntityReaderNode<EK,E,?,?,?> subNode = nodeByQualifierPrefix.get(qualifierPrefix);
			List<? extends Databean<?,?>> databeans = databeansByQualifierPrefix.get(qualifierPrefix);
			entity.addDatabeansForQualifierPrefixUnchecked(subNode.getEntityNodePrefix(), databeans);
		}
		// TODO add empty collections for empty prefixes since we were supposed to get all sub-entities
		return entity;
	}

	private Map<String,List<? extends Databean<?,?>>> getDatabeansByQualifierPrefix(Result row){
		if(row == null){
			return Collections.emptyMap();
		}
		Map<String,List<Cell>> cellsByQp = getKvsByQualifierPrefix(row);
		Map<String,List<? extends Databean<?,?>>> databeansByQp = new HashMap<>();
		for(Map.Entry<String,List<Cell>> entry : cellsByQp.entrySet()){
			String qp = entry.getKey();
			HBaseSubEntityReaderNode<EK,E,?,?,?> subNode = nodeByQualifierPrefix.get(qp);
			if(subNode == null){
				continue;// hopefully just orphaned data
			}
			HBaseSubEntityResultParser<EK,E,? extends EntityPrimaryKey<EK,?>,?,?> subParser = subNode.getResultParser();
			List<? extends Databean<?,?>> databeansForQp = subParser.getDatabeansForKvsWithMatchingQualifierPrefix(entry
					.getValue(), null);
			databeansByQp.put(qp, databeansForQp);
		}
		return databeansByQp;
	}

	private Map<String,List<Cell>> getKvsByQualifierPrefix(Result row){
		Map<String,List<Cell>> cellsByQp = new HashMap<>();
		List<Cell> cellsForQp = new ArrayList<>();
		String previousQp = null;
		for(Cell cell : DrIterableTool.nullSafe(row.listCells())){// row.list() can return null
			String qp = getQualifierPrefix(cell);
			if(DrObjectTool.notEquals(previousQp, qp)){
				cellsForQp = new ArrayList<>();
				cellsByQp.put(qp, cellsForQp);
				previousQp = qp;
			}
			cellsForQp.add(cell);
		}
		return cellsByQp;
	}

	private String getQualifierPrefix(Cell cell){
		int backingArrayOffset = cell.getQualifierOffset();
		int qualifierPrefixLength = getQualifierPrefixLength(cell);
		return StringByteTool.fromUtf8Bytes(cell.getValueArray(), backingArrayOffset, qualifierPrefixLength);
	}

	private int getQualifierPrefixLength(Cell cell){
		int qualifierOffset = cell.getQualifierOffset();
		int qualifierLength = cell.getQualifierLength();
		byte[] buffer = cell.getValueArray();
		for(int prefixLength = 0; prefixLength < qualifierLength; ++prefixLength){
			if(buffer[qualifierOffset + prefixLength] == EntityFieldInfo.ENTITY_PREFIX_TERMINATOR){
				return prefixLength;
			}
		}
		throw new IllegalArgumentException("couldn't find entity prefix termination byte");
	}

}

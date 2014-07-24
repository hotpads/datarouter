package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public class HBaseEntityResultParser<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{


	private Map<String,HBaseSubEntityReaderNode<EK,E,?,?,?>> nodeByQualifierPrefix;
	private HBaseSubEntityReaderNode<EK,E,?,?,?> anyNode;

	public HBaseEntityResultParser(Map<String,HBaseSubEntityReaderNode<EK,E,?,?,?>> nodeByQualifierPrefix){
		this.nodeByQualifierPrefix = nodeByQualifierPrefix;
		this.anyNode = MapTool.getFirstValue(nodeByQualifierPrefix);
	}
	
	public E parseEntity(EK ek, Result row){
		Class<E> entityClass = (Class<E>)anyNode.getFieldInfo().getEntityClass();
		E entity = ReflectionTool.create(entityClass);
		entity.setKey(ek);
		Map<String,Map<? extends EntityPrimaryKey<EK,?>,? extends Databean<?,?>>> databeansByQualifierPrefix
				= getDatabeansByQualifierPrefix(row);
		for(String qualifierPrefix : MapTool.nullSafe(databeansByQualifierPrefix).keySet()){
			Map<? extends EntityPrimaryKey<EK,?>,? extends Databean<?,?>> databeanByPk = databeansByQualifierPrefix.get(
					qualifierPrefix);
			HBaseSubEntityReaderNode<EK,E,?,?,?> subNode = nodeByQualifierPrefix.get(qualifierPrefix);
			entity.addDatabeansForQualifierPrefixUnchecked(subNode.getEntityNodePrefix(), databeanByPk.values());
		}
		return entity;
	}
	
	public Map<String,Map<? extends EntityPrimaryKey<EK,?>,? extends Databean<?,?>>> getDatabeansByQualifierPrefix(
			Result row){
		if(row==null){ return new HashMap<>(); }
		Map<String,Map<? extends EntityPrimaryKey<EK,?>,? extends Databean<?,?>>> databeanByPkByQualifierPrefix
				= new HashMap<>();
		for(KeyValue kv : row.list()){
			String qualifierPrefix = getQualifierPrefix(kv);
			HBaseSubEntityReaderNode<EK,E,?,?,?> subNode = nodeByQualifierPrefix.get(qualifierPrefix);
			if(subNode==null){ continue; }//hopefully just orphaned data
			HBaseSubEntityResultParser<EK,E,? extends EntityPrimaryKey<EK,?>,?,?> subParser = subNode.getResultParser();
			Map/*tough generics*/ databeanByPk = databeanByPkByQualifierPrefix.get(qualifierPrefix);
			if(databeanByPk==null){
				databeanByPk = new HashMap<>();
				databeanByPkByQualifierPrefix.put(qualifierPrefix, databeanByPk);
			}
			subParser.addKeyValueToResultsUnchecked(databeanByPk, kv);
		}
		return databeanByPkByQualifierPrefix;
	}
	
	private String getQualifierPrefix(KeyValue kv){
		int backingArrayOffset = kv.getQualifierOffset();
		int qualifierPrefixLength = getQualifierPrefixLength(kv);
		return StringByteTool.fromUtf8Bytes(kv.getBuffer(), backingArrayOffset, qualifierPrefixLength);
	}
	
	private int getQualifierPrefixLength(KeyValue kv){
		int qualifierOffset = kv.getQualifierOffset();
		int qualifierLength = kv.getQualifierLength();
		byte[] buffer = kv.getBuffer();
		for(int prefixLength=0; prefixLength < qualifierLength; ++prefixLength){
			if(buffer[qualifierOffset + prefixLength] == EntityFieldInfo.ENTITY_PREFIX_TERMINATOR){
				return prefixLength;
			}
		}
		throw new IllegalArgumentException("couldn't find entity prefix termination byte");
	}
	
}

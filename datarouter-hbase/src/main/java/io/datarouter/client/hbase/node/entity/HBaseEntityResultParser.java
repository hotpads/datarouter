/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.hbase.node.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;

import io.datarouter.client.hbase.node.subentity.HBaseSubEntityReaderNode;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityResultParser;
import io.datarouter.client.hbase.util.HBaseEntityKeyTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.node.adapter.BaseAdapter;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalSubEntitySortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.entity.SubEntitySortedMapStorageReaderNode;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.lang.ObjectTool;

public class HBaseEntityResultParser<EK extends EntityKey<EK>,E extends Entity<EK>>{

	private final EntityFieldInfo<EK,E> entityFieldInfo;
	private final Map<String,? extends SubEntitySortedMapStorageReaderNode<EK,?,?,?>> nodeByQualifierPrefix;
	private final Supplier<EK> entityKeySupplier;
	private final int numPrefixBytes;
	private final List<Field<?>> ekFields;

	public HBaseEntityResultParser(
			EntityFieldInfo<EK,E> entityFieldInfo,
			Map<String,? extends SubEntitySortedMapStorageReaderNode<EK,?,?,?>> nodeByQualifierPrefix,
			Supplier<EK> entityKeySupplier,
			int numPrefixBytes,
			List<Field<?>> ekFields){
		this.entityFieldInfo = entityFieldInfo;
		this.nodeByQualifierPrefix = nodeByQualifierPrefix;
		this.entityKeySupplier = entityKeySupplier;
		this.numPrefixBytes = numPrefixBytes;
		this.ekFields = ekFields;
	}

	public E parseEntity(Result row){
		E entity = entityFieldInfo.getEntitySupplier().get();
		entity.setKey(getEkFromRowBytes(row.getRow()));
		Map<String,List<? extends Databean<?,?>>> databeansByQualifierPrefix = getDatabeansByQualifierPrefix(row);
		for(String qualifierPrefix : databeansByQualifierPrefix.keySet()){
			SubEntitySortedMapStorageReaderNode<EK,?,?,?> subNode = nodeByQualifierPrefix.get(qualifierPrefix);
			List<? extends Databean<?,?>> databeans = databeansByQualifierPrefix.get(qualifierPrefix);
			entity.addDatabeansForQualifierPrefixUnchecked(subNode.getEntityNodePrefix(), databeans);
		}
		// TODO add empty collections for empty prefixes since we were supposed to get all sub-entities
		return entity;
	}

	public EK getEkFromRowBytes(byte[] rowBytes){
		return HBaseEntityKeyTool.getEkFromRowBytes(rowBytes, entityKeySupplier, numPrefixBytes, ekFields);
	}

	@SuppressWarnings("unchecked")
	private <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	Map<String,List<? extends Databean<?,?>>> getDatabeansByQualifierPrefix(Result row){
		if(row == null){
			return Collections.emptyMap();
		}
		Map<String,List<Cell>> cellsByQp = getKvsByQualifierPrefix(row);
		Map<String,List<? extends Databean<?,?>>> databeansByQp = new HashMap<>();
		for(Entry<String,List<Cell>> entry : cellsByQp.entrySet()){
			String qp = entry.getKey();
			SubEntitySortedMapStorageReaderNode<EK,?,?,?> subNode = nodeByQualifierPrefix.get(qp);
			if(subNode instanceof BaseAdapter){
				subNode = ((PhysicalSubEntitySortedMapStorageCallsiteAdapter<EK,?,?,?,? extends
						SubEntitySortedMapStorageReaderNode<EK,?,?,?>>)subNode).getUnderlyingNode();
			}
			HBaseSubEntityReaderNode<EK,?,?,?,?> hbaseNode = (HBaseSubEntityReaderNode<EK,?,?,?,?>)subNode;
			if(hbaseNode == null){
				continue;// hopefully just orphaned data
			}
			HBaseSubEntityResultParser<EK,? extends EntityPrimaryKey<EK,?>,?> subParser = hbaseNode
					.getResultParser();
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
		List<Cell> cells = row.listCells();// row.list() can return null
		if(cells == null){
			return cellsByQp;
		}
		for(Cell cell : cells){
			String qp = getQualifierPrefix(cell);
			if(ObjectTool.notEquals(previousQp, qp)){
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
		return StringByteTool.fromUtf8Bytes(cell.getQualifierArray(), backingArrayOffset, qualifierPrefixLength);
	}

	private int getQualifierPrefixLength(Cell cell){
		int qualifierOffset = cell.getQualifierOffset();
		int qualifierLength = cell.getQualifierLength();
		byte[] buffer = cell.getQualifierArray();
		for(int prefixLength = 0; prefixLength < qualifierLength; ++prefixLength){
			if(buffer[qualifierOffset + prefixLength] == EntityFieldInfo.ENTITY_PREFIX_TERMINATOR){
				return prefixLength;
			}
		}
		throw new IllegalArgumentException("couldn't find entity prefix termination byte");
	}

}

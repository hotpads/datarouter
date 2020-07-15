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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.util.HBaseResultScannerTool;
import io.datarouter.client.hbase.util.HBaseTableTool;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.entity.BasePhysicalEntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.util.DatarouterCounters;

public class HBaseEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BasePhysicalEntityNode<EK,E>{

	private static final int DEFAULT_GET_KEYS_LIMIT = 1000;

	protected final NodeFactory nodeFactory;
	protected final EntityNodeParams<EK,E> entityNodeParams;
	private final ClientTableNodeNames clientTableNodeNames;//currently acting as a cache of superclass fields
	private final HBaseEntityQueryBuilder<EK,E> queryBuilder;
	private final HBaseEntityResultParser<EK,E> resultParser;
	private final HBaseClientManager hBaseClientManager;
	private final ClientType<?,?> clientType;

	public HBaseEntityNode(
			HBaseClientManager hBaseClientManager,
			NodeFactory nodeFactory,
			EntityNodeParams<EK,E> entityNodeParams,
			ClientTableNodeNames clientTableNodeNames,
			ClientType<?,?> clientType){
		super(entityNodeParams, clientTableNodeNames);
		this.hBaseClientManager = hBaseClientManager;
		this.nodeFactory = nodeFactory;
		this.entityNodeParams = entityNodeParams;
		this.clientTableNodeNames = clientTableNodeNames;
		this.queryBuilder = new HBaseEntityQueryBuilder<>(getEntityFieldInfo());
		this.resultParser = new HBaseEntityResultParser<>(
				entityFieldInfo,
				getNodeByQualifierPrefix(),
				entityFieldInfo.getEntityKeySupplier(),
				entityFieldInfo.getEntityPartitioner().getNumPrefixBytes(),
				entityFieldInfo.getSampleEntityKey().getFields());
		this.clientType = clientType;
	}

	@Override
	public List<E> getEntities(Collection<EK> entityKeys, Config config){
		if(entityKeys == null || entityKeys.isEmpty()){
			return List.of();
		}
		List<Get> gets = entityKeys.stream()
				.map(queryBuilder::getRowBytesWithPartition)
				.map(Get::new)
				.collect(Collectors.toList());
		Result[] hbaseResults;
		try(Table table = getTable()){
			hbaseResults = HBaseTableTool.getUnchecked(table, gets);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		List<E> entities = Arrays.stream(hbaseResults)
				.filter(result -> !result.isEmpty())
				.map(resultParser::parseEntity)
				.filter(Entity::notEmpty)//not sure if this is necessary
				.sorted()
				.collect(Collectors.toList());
		long numDatabeans = entities.stream()
				.mapToLong(Entity::getNumDatabeans)
				.sum();
		DatarouterCounters.incClientNodeCustom(clientType, "entity databeans", getClientName(), clientTableNodeNames
				.getNodeName(), numDatabeans);
		return entities;
	}

	@Override
	public void deleteMultiEntities(Collection<EK> eks, Config config){
		Scanner.of(eks)
				.map(queryBuilder::getRowBytesWithPartition)
				.map(Delete::new)
				.batch(config.findInputBatchSize().orElse(100))
				.forEach(deletes -> {
					try(Table table = getTable()){
						HBaseTableTool.deleteUnchecked(table, deletes);
					}catch(IOException e){
						throw new RuntimeException(e);
					}
				});
	}

	@Override
	public List<EK> listEntityKeys(EK startKey, boolean startKeyInclusive, Config config){
		int limit = config.findLimit().orElse(DEFAULT_GET_KEYS_LIMIT);
		return queryBuilder.getScanForEachPartition(startKey, startKeyInclusive, true).stream()
				.map(scan -> {
					try(Table table = getTable();
						ResultScanner resultScanner = HBaseTableTool.getResultScanner(table, scan)){
						return HBaseResultScannerTool.resultScannerNext(resultScanner, limit);
					}catch(IOException e){
						throw new RuntimeException(e);
					}
				})
				.flatMap(List::stream)
				.map(Result::getRow)
				.map(resultParser::getEkFromRowBytes)
				.sorted()
				.limit(limit)
				.collect(Collectors.toList());
	}

	private Table getTable(){
		return hBaseClientManager.getTable(clientTableNodeNames.getClientId(), clientTableNodeNames.getTableName());
	}

}

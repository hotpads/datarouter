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
package io.datarouter.client.hbase.node.subentity;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.config.DatarouterHBaseExecutors.DatarouterHbaseClientExecutor;
import io.datarouter.client.hbase.node.nonentity.HBaseNonEntityQueryBuilder;
import io.datarouter.client.hbase.node.nonentity.HBaseNonEntityResultParser;
import io.datarouter.client.hbase.util.HBaseResultComparator;
import io.datarouter.client.hbase.util.HBaseTableTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.ScannerConfigTool;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.SubEntitySortedMapStorageReaderNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Range;

public class HBaseSubEntityReaderNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements SubEntitySortedMapStorageReaderNode<EK,PK,D,F>{
	private static final Logger logger = LoggerFactory.getLogger(HBaseSubEntityReaderNode.class);

	private static final Set<String> TABLE_NAMES = new HashSet<>();

	private final ClientTableNodeNames clientTableNodeNames;
	private final HBaseClientManager hBaseClientManager;
	private final EntityFieldInfo<EK,E> entityFieldInfo;
	private final EntityPartitioner<EK> partitioner;
	private final HBaseSubEntityResultParser<EK,PK,D> resultParser;
	private final HBaseNonEntityResultParser<EK,PK,D,F> nonEntityResultParser;
	private final ClientType<?,?> clientType;
	private final DatarouterHbaseClientExecutor datarouterHbaseClientExecutor;
	protected final HBaseSubEntityQueryBuilder<EK,E,PK,D,F> queryBuilder;
	protected final HBaseNonEntityQueryBuilder<EK,PK,D> nonEntityQueryBuilder;//for PageScanner
	private final HBaseResultComparator resultComparator;

	public HBaseSubEntityReaderNode(
			HBaseClientManager hBaseClientManager,
			EntityNodeParams<EK,E> entityNodeParams,
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			DatarouterHbaseClientExecutor datarouterHbaseClientExecutor){
		super(params, clientType);
		Require.isTrue(StringTool.notEmpty(getFieldInfo().getEntityNodePrefix()), "missing entityNodePrefix for "
				+ this);
		this.hBaseClientManager = hBaseClientManager;
		this.clientType = clientType;
		this.datarouterHbaseClientExecutor = datarouterHbaseClientExecutor;
		this.clientTableNodeNames = new ClientTableNodeNames(getFieldInfo().getClientId(), getFieldInfo()
				.getTableName(), getName());
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.partitioner = entityFieldInfo.getEntityPartitioner();
		this.queryBuilder = new HBaseSubEntityQueryBuilder<>(entityFieldInfo, getFieldInfo());
		this.nonEntityQueryBuilder = new HBaseNonEntityQueryBuilder<>(partitioner);
		this.resultParser = HBaseSubEntityResultParserFactory.create(entityFieldInfo, getFieldInfo());
		this.nonEntityResultParser = new HBaseNonEntityResultParser<>(partitioner, getFieldInfo());
		this.resultComparator = new HBaseResultComparator(partitioner.getNumPrefixBytes());
		logWideRows();
	}

	private void logWideRows(){
		String tableName = getFieldInfo().getTableName();
		if(!getFieldInfo().isSingleDatabeanEntity() && TABLE_NAMES.add(tableName)){
			logger.warn("potentially large rows in {}", tableName);
		}
	}

	/*---------------------------- plumbing ---------------------------------*/

	@Override
	public String getEntityNodePrefix(){
		return getFieldInfo().getEntityNodePrefix();
	}

	/*---------------------------- map storage reader -----------------------*/

	@Override
	public boolean exists(PK key, Config config){
		//should probably make a getKey method
		return get(key, config) != null;
	}

	@Override
	public D get(PK key, Config config){
		if(key == null){
			return null;
		}
		return getMulti(List.of(key), config).stream().findFirst().orElse(null);
	}

	@Override
	public List<D> getMulti(Collection<PK> pks, Config config){
		if(pks == null || pks.isEmpty()){
			return Collections.emptyList();
		}
		String clientName = getClientId().getName();
		String nodeName = getName();
		DatarouterCounters.incClientNodeCustom(clientType, "getMulti requested", clientName, nodeName, pks.size());
		List<Get> gets = queryBuilder.getGets(pks, false);
		Result[] hbaseResults;
		try(Table table = getTable()){
			hbaseResults = HBaseTableTool.getUnchecked(table, gets);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		List<D> databeans = resultParser.getDatabeansWithMatchingQualifierPrefixMulti(hbaseResults);
		DatarouterCounters.incClientNodeCustom(clientType, "getMulti found", clientName, nodeName, databeans.size());
		return databeans;
	}

	@Override
	public List<PK> getKeys(Collection<PK> pks, Config config){
		if(pks == null || pks.isEmpty()){
			return Collections.emptyList();
		}
		String clientName = getClientId().getName();
		String nodeName = getName();
		DatarouterCounters.incClientNodeCustom(clientType, "getKeys requested", clientName, nodeName, pks.size());
		List<Get> gets = queryBuilder.getGets(pks, true);
		Result[] hbaseResults;
		try(Table table = getTable()){
			hbaseResults = HBaseTableTool.getUnchecked(table, gets);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		List<PK> result = resultParser.getPrimaryKeysWithMatchingQualifierPrefixMulti(hbaseResults);
		DatarouterCounters.incClientNodeCustom(clientType, "getKeys found", clientName, nodeName, result.size());
		return result;
	}

	/*---------------------------- sorted -----------------------------------*/

	@Override
	public Scanner<PK> scanKeys(Range<PK> range, Config config){
		Config subscanConfig = config.clone().setOffset(0);
		Scanner<PK> scanner = makePageScanner(range, subscanConfig, true)
				.map(resultParser::getPrimaryKeysWithMatchingQualifierPrefix)
				.concat(Scanner::of)
				.deduplicate()
				.include(pk -> KeyRangeTool.contains(range, pk));
		return ScannerConfigTool.applyOffsetAndLimit(scanner, config);
	}

	@Override
	public Scanner<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		Config subscanConfig = config.clone().setOffset(0);
		Scanner<PK> scanner = Scanner.of(ranges)
				.collate(range -> scanKeys(range, subscanConfig));
		return ScannerConfigTool.applyOffsetAndLimit(scanner, config);
	}

	@Override
	public Scanner<D> scan(Range<PK> range, Config config){
		Config subscanConfig = config.clone().setOffset(0);
		Scanner<D> scanner = makePageScanner(range, subscanConfig, false)
				.map(result -> resultParser.getDatabeansWithMatchingQualifierPrefix(result, null))
				.concat(Scanner::of)
				.include(databean -> KeyRangeTool.contains(range, databean.getKey()));
		return ScannerConfigTool.applyOffsetAndLimit(scanner, config);
	}

	@Override
	public Scanner<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		Config subscanConfig = config.clone().setOffset(0);
		Scanner<D> scanner = Scanner.of(ranges)
				.collate(range -> scan(range, subscanConfig));
		return ScannerConfigTool.applyOffsetAndLimit(scanner, config);
	}

	/*------------- scanner factory -----------------*/

	public Scanner<Result> makePageScanner(Range<PK> range, Config config, boolean keysOnly){
		HBaseSubEntityPageScanner<EK,PK,D,F> pageScanner = new HBaseSubEntityPageScanner<>(
				clientType,
				clientTableNodeNames.getClientId(),
				clientTableNodeNames.getTableName(),
				clientTableNodeNames.getNodeName(),
				hBaseClientManager,
				datarouterHbaseClientExecutor,
				getFieldInfo(),
				partitioner,
				nonEntityQueryBuilder,
				resultComparator,
				nonEntityResultParser);
		return pageScanner.scanResults(range, config, keysOnly);
	}

	/*----------- getter -------------*/

	public HBaseSubEntityResultParser<EK,PK,D> getResultParser(){
		return resultParser;
	}

	public ClientTableNodeNames getClientTableNodeNames(){
		return clientTableNodeNames;
	}

	public EntityFieldInfo<EK,E> getEntityFieldInfo(){
		return entityFieldInfo;
	}

	protected Table getTable(){
		return hBaseClientManager.getTable(getClientId(), clientTableNodeNames.getTableName());
	}

}

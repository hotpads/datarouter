/*
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
package io.datarouter.storage.test.node.basic.sorted;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSortedNodeTestDao extends BaseDao implements TestDao{

	private final SortedMapStorage<SortedBeanKey,SortedBean> node;

	public DatarouterSortedNodeTestDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			ClientId clientId,
			Optional<String> tableName){
		super(datarouter);
		node = nodeFactory.create(clientId, SortedBeanEntityKey::new, SortedBean::new, SortedBeanFielder::new)
				.withTableName(tableName.orElse("SortedBean"))
				.buildAndRegister();
	}

	/*-------------------------------- entity -------------------------------*/

	public SortedMapStorage<SortedBeanKey,SortedBean> getNode(){
		return node;
	}

	/*---------------------------- sub entity -------------------------------*/

	public long count(){
		return node.count(Range.everything());
	}

	public boolean exists(SortedBeanKey key){
		return node.exists(key);
	}

	public boolean exists(SortedBeanKey key, Config config){
		return node.exists(key, config);
	}

	/*-------------------------------- get ----------------------------------*/

	public SortedBean get(SortedBeanKey key){
		return node.get(key);
	}

	public SortedBean get(SortedBeanKey key, Config config){
		return node.get(key, config);
	}

	public List<SortedBean> getMulti(Collection<SortedBeanKey> keys, int batchSize){
		return node.getMulti(keys, new Config().setRequestBatchSize(batchSize));
	}

	public List<SortedBeanKey> getKeys(Collection<SortedBeanKey> keys){
		return node.getKeys(keys);
	}

	/*-------------------------------- put ----------------------------------*/

	public void put(SortedBean databean){
		node.put(databean);
	}

	public void put(SortedBean databean, Config config){
		node.put(databean, config);
	}

	public void putMulti(Collection<SortedBean> databeans){
		node.putMulti(databeans);
	}

	public void putMulti(Collection<SortedBean> databeans, Config config){
		node.putMulti(databeans, config);
	}

	public void putStream(Stream<SortedBean> databeans){
		Scanner.of(databeans)
				.batch(100)
				.forEach(node::putMulti);
	}

	/*-------------------------------- scan ---------------------------------*/

	public Scanner<SortedBean> scan(){
		return node.scan();
	}

	public Scanner<SortedBean> scan(int batchSize){
		var config = new Config().setResponseBatchSize(batchSize);
		return node.scan(config);
	}

	public Scanner<SortedBean> scan(Config config){
		return node.scan(config);
	}

	public Scanner<SortedBean> scan(Range<SortedBeanKey> range){
		return node.scan(range);
	}

	public Scanner<SortedBean> scan(Range<SortedBeanKey> range, Config config){
		return node.scan(range, config);
	}

	public Scanner<SortedBeanKey> scanKeys(){
		return node.scanKeys();
	}

	public Scanner<SortedBeanKey> scanKeys(Config config){
		return node.scanKeys(config);
	}

	public Scanner<SortedBeanKey> scanKeys(Range<SortedBeanKey> range){
		return node.scanKeys(range);
	}

	public Scanner<SortedBeanKey> scanKeys(Range<SortedBeanKey> range, Config config){
		return node.scanKeys(range, config);
	}

	public Scanner<SortedBean> scanRanges(List<Range<SortedBeanKey>> ranges){
		return node.scanRanges(ranges);
	}

	public Scanner<SortedBean> scanRanges(List<Range<SortedBeanKey>> ranges, Config config){
		return node.scanRanges(ranges, config);
	}

	public Scanner<SortedBean> scanWithPrefix(SortedBeanKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public Scanner<SortedBean> scanWithPrefix(SortedBeanKey prefix, int batchSize){
		Config config = new Config().setResponseBatchSize(batchSize);
		return node.scanWithPrefix(prefix, config);
	}

	public Scanner<SortedBean> scanWithPrefix(SortedBeanKey prefix, Config config){
		return node.scanWithPrefix(prefix, config);
	}

	public Scanner<SortedBeanKey> scanKeysWithPrefix(SortedBeanKey prefix){
		return node.scanKeysWithPrefix(prefix);
	}

	public Scanner<SortedBeanKey> scanKeysWithPrefix(SortedBeanKey prefix, Config config){
		return node.scanKeysWithPrefix(prefix, config);
	}

	public Scanner<SortedBean> scanWithPrefixes(List<SortedBeanKey> prefixes){
		return node.scanWithPrefixes(prefixes);
	}

	/*------------------------------- delete --------------------------------*/

	public void deleteAll(){
		node.deleteAll();
	}

	public void delete(SortedBeanKey key){
		node.delete(key);
	}

	public void deleteMulti(Collection<SortedBeanKey> keys){
		node.deleteMulti(keys);
	}

	public void deleteMulti(Collection<SortedBeanKey> keys, Config config){
		node.deleteMulti(keys, config);
	}

	public void deleteWithPrefix(SortedBeanKey prefix){
		node.deleteWithPrefix(prefix);
	}

}

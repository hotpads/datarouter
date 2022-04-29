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
package io.datarouter.storage.test.node.type.index.node;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.IndexingNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedMapStorage;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.type.index.UniqueIndexNode;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar.TestDatabeanWithManagedIndexByBFielder;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBarKey;

public class DatarouterTestDatabeanWithIndexDao extends BaseDao implements TestDao{

	private final IndexedMapStorage<TestDatabeanKey,TestDatabean> backingMapNode;
	private final MapStorage<TestDatabeanKey,TestDatabean> mainNode;
	private final UniqueIndexNode<
			TestDatabeanKey,
			TestDatabean,
			TestDatabeanWithManagedIndexByBarKey,
			TestDatabeanWithManagedIndexByBar> byB;

	public DatarouterTestDatabeanWithIndexDao(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId,
			String tableName, boolean manageTxn, String indexName){
		super(datarouter);
		mainNode = backingMapNode = nodeFactory.create(clientId, TestDatabean::new, TestDatabeanFielder::new)
				.withTableName(tableName)
				.buildAndRegister();
		byB = backingMapNode.registerManaged(IndexingNodeFactory.newManagedUnique(backingMapNode,
				TestDatabeanWithManagedIndexByBFielder::new, TestDatabeanWithManagedIndexByBar::new, manageTxn,
				indexName));
	}

	public void putMulti(Collection<TestDatabean> databeans){
		mainNode.putMulti(databeans);
	}

	public void deleteAll(){
		mainNode.deleteAll();
	}

	public TestDatabean lookupUnique(TestDatabeanWithManagedIndexByBarKey key){
		return byB.lookupUnique(key);
	}

	public List<TestDatabean> lookupMultiUnique(Collection<TestDatabeanWithManagedIndexByBarKey> keys){
		return byB.lookupMultiUnique(keys);
	}

	public TestDatabeanWithManagedIndexByBar get(TestDatabeanWithManagedIndexByBarKey key){
		return byB.get(key);
	}

	public TestDatabean get(TestDatabeanKey key){
		return mainNode.get(key);
	}

	public List<TestDatabean> getMulti(Collection<TestDatabeanKey> keys){
		return mainNode.getMulti(keys);
	}

	public List<TestDatabeanWithManagedIndexByBar> getMultiByB(Collection<TestDatabeanWithManagedIndexByBarKey> keys){
		return byB.getMulti(keys);
	}

	public void deleteUnique(TestDatabeanWithManagedIndexByBarKey key){
		byB.deleteUnique(key);
	}

	public void deleteMultiUnique(Collection<TestDatabeanWithManagedIndexByBarKey> keys){
		byB.deleteMultiUnique(keys);
	}

	public void put(TestDatabean databean){
		mainNode.put(databean);
	}

	public Scanner<TestDatabeanWithManagedIndexByBar> scanByB(){
		return byB.scan();
	}

	public Scanner<TestDatabean> scanDatabeansByB(){
		return byB.scanDatabeans();
	}

	public Scanner<TestDatabeanWithManagedIndexByBarKey> scanKeysByB(Optional<Integer> limit){
		Config config = limit.map(intLimit -> new Config().setLimit(intLimit)).orElse(new Config());
		return byB.scanKeys(config);
	}

}

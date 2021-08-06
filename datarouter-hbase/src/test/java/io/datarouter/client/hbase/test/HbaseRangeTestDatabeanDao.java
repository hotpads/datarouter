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
package io.datarouter.client.hbase.test;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.base.BaseByteArrayEntityPartitioner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.util.HashMethods;
import io.datarouter.util.tuple.Range;

@Singleton
public class HbaseRangeTestDatabeanDao extends BaseDao implements TestDao{

	private final SortedMapStorage<TestDatabeanKey,TestDatabean> node;

	private static class HbaseRangeTestDatabeanPartitioner extends BaseByteArrayEntityPartitioner<TestDatabeanKey>{

		public HbaseRangeTestDatabeanPartitioner(){
			super(HashMethods::longDjbHash, 2);
		}

		@Override
		public byte[] makeByteArrayHashInput(TestDatabeanKey ek){
			return FieldTool.getPartitionerInput(ek.getFields());
		}

	}

	@Inject
	public HbaseRangeTestDatabeanDao(Datarouter datarouter, NodeFactory nodeFactory){
		super(datarouter);
		node = nodeFactory.create(
				DatarouterHBaseTestClientIds.HBASE,
				TestDatabean::new,
				TestDatabeanFielder::new)
				.withPartitionerSupplier(HbaseRangeTestDatabeanPartitioner::new)
				.withTableName("HbaseRangeIntegrationTests")
				.buildAndRegister();
	}

	public void putMulti(Collection<TestDatabean> databeans){
		node.putMulti(databeans);
	}

	public Scanner<TestDatabeanKey> scanKeys(Range<TestDatabeanKey> range){
		return node.scanKeys(range);
	}

	public void deleteAll(){
		node.deleteAll();
	}

}

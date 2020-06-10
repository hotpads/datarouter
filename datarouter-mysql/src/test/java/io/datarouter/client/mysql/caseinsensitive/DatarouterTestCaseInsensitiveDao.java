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
package io.datarouter.client.mysql.caseinsensitive;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.caseinsensitive.CaseInsensitiveTestDatabean.CaseInsensitiveTestFielder;
import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.util.tuple.Range;

@Singleton
public class DatarouterTestCaseInsensitiveDao extends BaseDao implements TestDao{

	private final SortedMapStorage<CaseInsensitiveTestPrimaryKey,CaseInsensitiveTestDatabean> node;

	@Inject
	public DatarouterTestCaseInsensitiveDao(Datarouter datarouter, NodeFactory nodeFactory){
		super(datarouter);
		node = nodeFactory.create(
				DatarouterMysqlTestClientids.MYSQL,
				CaseInsensitiveTestDatabean::new,
				CaseInsensitiveTestFielder::new)
				.buildAndRegister();
	}

	public void put(CaseInsensitiveTestDatabean databean){
		node.put(databean);
	}

	public Scanner<CaseInsensitiveTestDatabean> scanRanges(
			List<Range<CaseInsensitiveTestPrimaryKey>> ranges,
			int outputBatchSize){
		return node.scanRanges(ranges, new Config().setOutputBatchSize(outputBatchSize));
	}

}

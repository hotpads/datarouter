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
package io.datarouter.exception.storage.summary;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.storage.summary.ExceptionRecordSummary.ExceptionRecordSummaryFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterExceptionRecordSummaryDao extends BaseDao{

	public static class DatarouterExceptionRecordSummaryDaoParams extends BaseDaoParams{

		public DatarouterExceptionRecordSummaryDaoParams(ClientId clientId){
			super(clientId);
		}
	}

	private final SortedMapStorage<ExceptionRecordSummaryKey,ExceptionRecordSummary> node;

	@Inject
	public DatarouterExceptionRecordSummaryDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterExceptionRecordSummaryDaoParams params){
		super(datarouter);
		node = nodeFactory.create(
				params.clientId,
				ExceptionRecordSummary::new,
				ExceptionRecordSummaryFielder::new)
				.buildAndRegister();
	}

	public Scanner<ExceptionRecordSummary> scan(){
		return node.scan();
	}

	public Scanner<ExceptionRecordSummaryKey> scanKeys(int limit){
		return node.scanKeys(new Config().setLimit(limit));
	}

	public void putMulti(Collection<ExceptionRecordSummary> databeans){
		node.putMulti(databeans);
	}

}

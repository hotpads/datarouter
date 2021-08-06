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
package io.datarouter.exception.storage.summary;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.storage.summary.ExceptionRecordSummary.ExceptionRecordSummaryFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterExceptionRecordSummaryDao extends BaseDao{

	public static class DatarouterExceptionRecordSummaryDaoParams extends BaseRedundantDaoParams{

		public DatarouterExceptionRecordSummaryDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}
	}

	private final SortedMapStorageNode<ExceptionRecordSummaryKey,ExceptionRecordSummary,
			ExceptionRecordSummaryFielder> node;

	@Inject
	public DatarouterExceptionRecordSummaryDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterExceptionRecordSummaryDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<ExceptionRecordSummaryKey,ExceptionRecordSummary,
							ExceptionRecordSummaryFielder> node =
							nodeFactory.create(clientId, ExceptionRecordSummary::new,
									ExceptionRecordSummaryFielder::new)
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::new);
		datarouter.register(node);
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

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
package io.datarouter.loggerconfig.storage.fileappender;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.loggerconfig.storage.fileappender.FileAppender.FileAppenderFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterFileAppenderDao extends BaseDao{

	public static class DatarouterFileAppenderDaoParams extends BaseRedundantDaoParams{

		public DatarouterFileAppenderDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<FileAppenderKey,FileAppender,FileAppenderFielder> node;

	@Inject
	public DatarouterFileAppenderDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterFileAppenderDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<FileAppenderKey,FileAppender,FileAppenderFielder> node =
							nodeFactory.create(clientId, FileAppender::new, FileAppenderFielder::new)
						.withIsSystemTable(true)
						.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void createAndputFileAppender(String name, String pattern, String fileName){
		var appender = new FileAppender(name, pattern, fileName);
		node.put(appender);
	}

	public void deleteFileAppender(String name){
		node.delete(new FileAppenderKey(name));
	}

	public Scanner<FileAppender> scan(){
		return node.scan();
	}

}

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
package io.datarouter.loggerconfig.storage.consoleappender;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

import io.datarouter.loggerconfig.storage.consoleappender.ConsoleAppender.ConsoleAppenderFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterConsoleAppenderDao extends BaseDao{

	public static class DatarouterConsoleAppenderDaoParams extends BaseRedundantDaoParams{

		public DatarouterConsoleAppenderDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<ConsoleAppenderKey,ConsoleAppender,ConsoleAppenderFielder> node;

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Inject
	public DatarouterConsoleAppenderDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterConsoleAppenderDaoParams params){
		super(datarouter);
		node = new RedundantSortedMapStorageNode(Scanner.of(params.clientIds)
				.map(clientId -> nodeFactory.create(clientId, ConsoleAppender::new, ConsoleAppenderFielder::new)
						.withIsSystemTable(true)
						.buildAndRegister())
				.list());
		datarouter.register(node);
	}

	public Scanner<ConsoleAppender> scan(){
		return node.scan();
	}

	public void createAndPutConsoleAppender(String name, String pattern, Target target){
		var appender = new ConsoleAppender(name, pattern, target);
		node.put(appender);
	}

	public void deleteConsoleAppender(String name){
		node.delete(new ConsoleAppenderKey(name));
	}

}

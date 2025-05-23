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
package io.datarouter.webappinstance.storage.webappinstancelog;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.storage.vacuum.DatabeanVacuum;
import io.datarouter.storage.vacuum.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog.WebappInstanceLogFielder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebappInstanceLogDao extends BaseDao{

	public record DatarouterWebappInstanceLogDaoParams(List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<WebappInstanceLogKey,WebappInstanceLog,WebappInstanceLogFielder> node;

	@Inject
	public DatarouterWebappInstanceLogDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterWebappInstanceLogDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<WebappInstanceLogKey,WebappInstanceLog,WebappInstanceLogFielder> node =
							nodeFactory.create(clientId, WebappInstanceLog::new, WebappInstanceLogFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
					})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void put(WebappInstanceLog log){
		node.put(log);
	}

	public Scanner<WebappInstanceLog> scan(){
		return node.scan();
	}

	public Scanner<WebappInstanceLog> scanWithPrefix(WebappInstanceLogKey key){
		return node.scanWithPrefix(key);
	}

	public DatabeanVacuum<WebappInstanceLogKey,WebappInstanceLog> makeVacuum(){
		LocalDateTime deleteBeforeTime = LocalDateTime.now(Clock.systemUTC()).minusDays(30L);
		return new DatabeanVacuumBuilder<>(
				"DatarouterWebappInstanceLog",
				node.scan(),
				databean -> databean.getRefreshedLast().isBefore(deleteBeforeTime.toInstant(ZoneOffset.UTC)),
				node::deleteMulti)
				.build();
	}
}

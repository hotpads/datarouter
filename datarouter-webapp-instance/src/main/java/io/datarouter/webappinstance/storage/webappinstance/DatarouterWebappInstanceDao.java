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
package io.datarouter.webappinstance.storage.webappinstance;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance.WebappInstanceFielder;

@Singleton
public class DatarouterWebappInstanceDao extends BaseDao{

	public static class DatarouterWebappInstanceDaoParams extends BaseRedundantDaoParams{

		public DatarouterWebappInstanceDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<WebappInstanceKey,WebappInstance,WebappInstanceFielder> node;

	@Inject
	public DatarouterWebappInstanceDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterWebappInstanceDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<WebappInstanceKey,WebappInstance,WebappInstanceFielder> node =
							nodeFactory.create(clientId, WebappInstance::new, WebappInstanceFielder::new)
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::new);
		datarouter.register(node);
	}

	public Scanner<WebappInstanceKey> scanKeys(){
		return node.scanKeys();
	}

	public Scanner<WebappInstance> scan(){
		return node.scan();
	}

	public Scanner<WebappInstance> scanWithPrefix(WebappInstanceKey key){
		return node.scanWithPrefix(key);
	}

	public void put(WebappInstance webappInstance){
		node.put(webappInstance);
	}

	public void delete(WebappInstanceKey key){
		node.delete(key);
	}

	public WebappInstance get(WebappInstanceKey key){
		return node.get(key);
	}

	public List<String> getWebappInstanceServerNamesForWebapp(String webappName){
		return node.scanWithPrefix(new WebappInstanceKey(webappName, null))
				.map(WebappInstance::getKey)
				.map(WebappInstanceKey::getServerName)
				.list();
	}

	/**
	 * Callers should use {@link WebappInstance#getUniqueServerNames} on result if only serverNames are desired
	 * (not each webApp on the server)
	 */
	public List<WebappInstance> getWebappInstancesOfServerType(ServerType type, Duration heartbeatWithin){
		return getWebappInstancesWithServerTypeString(type.getPersistentString(), heartbeatWithin);
	}

	public List<WebappInstance> getWebappInstancesWithServerTypeString(String typeString, Duration heartbeatWithin){
		return node.scan()
				.include(webappInstance -> typeString.equals(webappInstance.getServerType()))
				.include(webappInstance -> webappInstance.getDurationSinceLastUpdatedMs()
						.compareTo(heartbeatWithin) < 0)
				.list();
	}

	public List<WebappInstance> getWebappInstancesByServerType(ServerType serverType){
		String serverTypeString = serverType.getPersistentString();
		return node.scan()
				.include(webappInstance -> serverTypeString.equals(webappInstance.getServerType()))
				.list();
	}

	public Map<String,String> getServerTypeByServerName(){
		return node.scan().toMap(app -> app.getKey().getServerName(), BaseWebappInstance::getServerType);
	}

	public DatabeanVacuum<WebappInstanceKey,WebappInstance> makeVacuum(){
		Instant deleteBefore = Instant.now().minus(Duration.ofMinutes(20));
		Predicate<WebappInstance> shouldDelete = databean -> databean.getRefreshedLastInstant() == null
				|| databean.getRefreshedLastInstant().isBefore(deleteBefore);
		return new DatabeanVacuumBuilder<>(node.scan(), shouldDelete, node::deleteMulti).build();
	}

}

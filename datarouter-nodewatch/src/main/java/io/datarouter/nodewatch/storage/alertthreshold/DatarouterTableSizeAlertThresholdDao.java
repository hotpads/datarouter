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
package io.datarouter.nodewatch.storage.alertthreshold;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThreshold.TableSizeAlertThresholdFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterTableSizeAlertThresholdDao extends BaseDao{

	public static class DatarouterTableSizeAlertThresholdDaoParams extends BaseDaoParams{

		public DatarouterTableSizeAlertThresholdDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<TableSizeAlertThresholdKey,TableSizeAlertThreshold> node;

	@Inject
	public DatarouterTableSizeAlertThresholdDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterTableSizeAlertThresholdDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, TableSizeAlertThreshold::new, TableSizeAlertThresholdFielder::new)
				.withIsSystemTable(true)
				.buildAndRegister();
	}

	public Optional<TableSizeAlertThreshold> find(TableSizeAlertThresholdKey key){
		return node.find(key);
	}

	public void put(TableSizeAlertThreshold databean){
		node.put(databean);
	}

	public void putMulti(Collection<TableSizeAlertThreshold> databeans){
		node.putMulti(databeans);
	}

}

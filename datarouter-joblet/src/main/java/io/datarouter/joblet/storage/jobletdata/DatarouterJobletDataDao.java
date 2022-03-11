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
package io.datarouter.joblet.storage.jobletdata;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.storage.jobletdata.JobletData.JobletDataFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterJobletDataDao extends BaseDao{

	public static class DatarouterJobletDataDaoParams extends BaseRedundantDaoParams{

		public DatarouterJobletDataDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}
	}

	private final SortedMapStorageNode<JobletDataKey,JobletData,JobletDataFielder> node;

	@Inject
	public DatarouterJobletDataDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterJobletDataDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<JobletDataKey,JobletData,JobletDataFielder> node =
							nodeFactory.create(clientId, JobletData::new, JobletDataFielder::new)
						.disableNodewatchPercentageAlert()
						.disableNodewatchThresholdAlert()
						.withTag(Tag.DATAROUTER)
						.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void delete(JobletDataKey key){
		node.delete(key);
	}

	public void deleteMulti(Collection<JobletDataKey> keys){
		node.deleteMulti(keys);
	}

	public JobletData get(JobletDataKey key){
		return node.get(key);
	}

	public List<JobletData> getMulti(Collection<JobletDataKey> keys){
		return node.getMulti(keys);
	}

	public void putMultiOrBust(Collection<JobletData> databeans){
		node.putMulti(databeans, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
	}

	public Scanner<JobletData> scan(){
		return node.scan();
	}

}

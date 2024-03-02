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
package io.datarouter.web.storage.payloadsampling.response;

import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import io.datarouter.web.storage.payloadsampling.PayloadSampleKey;
import io.datarouter.web.storage.payloadsampling.request.RequestPayloadSampleDao.PayloadSamplingDaoParams;
import io.datarouter.web.storage.payloadsampling.response.ResponsePayloadSample.ResponsePayloadSampleFielder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ResponsePayloadSampleDao extends BaseDao{

	private final SortedMapStorageNode<PayloadSampleKey,ResponsePayloadSample,ResponsePayloadSampleFielder> node;

	@Inject
	public ResponsePayloadSampleDao(Datarouter datarouter,
			NodeFactory nodeFactory,
			PayloadSamplingDaoParams params){
		super(datarouter);

		node = Scanner.of(params.clientIds())
				.map(clientId -> {
					SortedMapStorageNode<
							PayloadSampleKey,
							ResponsePayloadSample,
							ResponsePayloadSampleFielder> node = nodeFactory.create(
									clientId,
									ResponsePayloadSample::new,
									ResponsePayloadSampleFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public ResponsePayloadSample get(PayloadSampleKey key){
		return node.get(key);
	}

	public void put(ResponsePayloadSample payload){
		node.put(payload);
	}

	public void deleteMulti(List<PayloadSampleKey> keys){
		node.deleteMulti(keys);
	}

}

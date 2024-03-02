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
package io.datarouter.web.storage.payloadsampling.request;

import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import io.datarouter.web.storage.payloadsampling.PayloadSampleKey;
import io.datarouter.web.storage.payloadsampling.request.RequestPayloadSample.RequestPayloadSampleFielder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RequestPayloadSampleDao extends BaseDao{

	public record PayloadSamplingDaoParams(List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<PayloadSampleKey,RequestPayloadSample,RequestPayloadSampleFielder> node;

	@Inject
	public RequestPayloadSampleDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			PayloadSamplingDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<PayloadSampleKey,RequestPayloadSample,RequestPayloadSampleFielder> node =
							nodeFactory.create(clientId, RequestPayloadSample::new,
									RequestPayloadSampleFielder::new)
							.withTag(Tag.DATAROUTER)
							.withTableName("RequestPayloadSampleV2")
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public Scanner<RequestPayloadSample> scanMulti(List<PayloadSampleKey> keys){
		return node.scanMulti(keys);
	}

	public List<RequestPayloadSample> getMulti(List<PayloadSampleKey> keys){
		return node.getMulti(keys);
	}

	public Scanner<PayloadSampleKey> scanKeys(){
		return node.scanKeys();
	}

	public void put(RequestPayloadSample payload){
		node.put(payload);
	}

	public void deleteMulti(List<PayloadSampleKey> keys){
		node.deleteMulti(keys);
	}

}

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
package io.datarouter.gcp.spanner.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.gcp.spanner.SpannerClientManager;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.gcp.spanner.op.SpannerVacuum;
import io.datarouter.gcp.spanner.op.read.SpannerFindTallyOp;
import io.datarouter.gcp.spanner.op.write.SpannerDeleteOp;
import io.datarouter.gcp.spanner.op.write.SpannerIncrementOp;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

public class SpannerTallyNode
extends BasePhysicalNode<TallyKey,Tally,TallyFielder>
implements PhysicalTallyStorageNode{

	private final SpannerClientManager clientManager;
	private final SpannerFieldCodecs fieldCodecs;

	public SpannerTallyNode(
			NodeParams<TallyKey,Tally,TallyFielder> params,
			ClientType<?,?> clientType,
			SpannerClientManager clientManager,
			SpannerFieldCodecs fieldCodecs){
		super(params, clientType);
		this.clientManager = clientManager;
		this.fieldCodecs = fieldCodecs;
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		var incrementOp = new SpannerIncrementOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				key,
				Long.valueOf(delta),
				config);
		return incrementOp.wrappedCall();
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		var findOp = new SpannerFindTallyOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getTallyFieldInfo(),
				List.of(key),
				config);
		List<Tally> result = findOp.wrappedCall();
		return result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.get(0).getTally());
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		var findOp = new SpannerFindTallyOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getTallyFieldInfo(),
				keys,
				config);
		List<Tally> result = findOp.wrappedCall();
		return Scanner.of(result)
				.toMap(tally -> tally.getKey().getId(), Tally::getTally);
	}

	@Override
	public void deleteTally(String key, Config config){
		var op = new SpannerDeleteOp<>(
				clientManager.getDatabaseClient(getFieldInfo().getClientId()),
				getTallyFieldInfo(),
				List.of(new TallyKey(key)),
				config,
				fieldCodecs);
		op.wrappedCall();
	}

	private PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> getTallyFieldInfo(){
		return this.getFieldInfo();
	}

	@Override
	public void vacuum(Config config){
		var vacuum = new SpannerVacuum<>(
				clientManager.getDatabaseClient(getClientId()),
				getTallyFieldInfo(),
				TallyKey.FieldKeys.id.getColumnName(),
				config);
		vacuum.vacuum();
	}

}

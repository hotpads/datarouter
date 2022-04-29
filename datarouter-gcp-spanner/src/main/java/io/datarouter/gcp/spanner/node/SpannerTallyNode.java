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
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.op.read.SpannerGetOp;
import io.datarouter.gcp.spanner.op.write.SpannerDeleteOp;
import io.datarouter.gcp.spanner.op.write.SpannerIncrementOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
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

public class SpannerTallyNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalTallyStorageNode<PK,D,F>{

	private final SpannerClientManager clientManager;
	private final SpannerFieldCodecRegistry spannerFieldCodecRegistry;

	public SpannerTallyNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			SpannerClientManager clientManager,
			SpannerFieldCodecRegistry spannerFieldCodecRegistry){
		super(params, clientType);
		this.clientManager = clientManager;
		this.spannerFieldCodecRegistry = spannerFieldCodecRegistry;
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		var incrementOp = new SpannerIncrementOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getFieldInfo(),
				key,
				Long.valueOf(delta));
		return incrementOp.wrappedCall();
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		var getOp = new SpannerGetOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getTallyFieldInfo(),
				List.of(new TallyKey(key)),
				config,
				spannerFieldCodecRegistry);
		return getOp.wrappedCall()
				.stream()
				.findFirst()
				.map(Tally::getTally);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		List<TallyKey> tallyKeys = Scanner.of(keys)
				.map(TallyKey::new)
				.list();
		var getOp = new SpannerGetOp<>(
				clientManager.getDatabaseClient(getClientId()),
				getTallyFieldInfo(),
				tallyKeys,
				config,
				spannerFieldCodecRegistry);
		List<Tally> tallies = getOp.wrappedCall();
		return Scanner.of(tallies)
				.toMap(tally -> tally.getKey().getId(), Tally::getTally);
	}

	@Override
	public void deleteTally(String key, Config config){
		var op = new SpannerDeleteOp<>(
				clientManager.getDatabaseClient(getFieldInfo().getClientId()),
				getTallyFieldInfo(),
				List.of(new TallyKey(key)),
				config,
				spannerFieldCodecRegistry);
		op.wrappedCall();
	}

	@SuppressWarnings("unchecked")
	private PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> getTallyFieldInfo(){
		return (PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder>)this.getFieldInfo();
	}

}

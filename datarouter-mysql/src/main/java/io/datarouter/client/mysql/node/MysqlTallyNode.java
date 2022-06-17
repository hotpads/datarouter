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
package io.datarouter.client.mysql.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

public class MysqlTallyNode
extends BasePhysicalNode<TallyKey,Tally,TallyFielder>
implements PhysicalTallyStorageNode{

	private final MysqlNodeManager mysqlNodeManager;

	public MysqlTallyNode(
			NodeParams<TallyKey,Tally,TallyFielder> params,
			ClientType<?,?> clientType,
			MysqlNodeManager mysqlNodeManager){
		super(params, clientType);
		this.mysqlNodeManager = mysqlNodeManager;
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		return mysqlNodeManager.increment(
				getTallyFieldInfo(),
				key,
				(long)delta,
				config);
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		return mysqlNodeManager.findTally(getTallyFieldInfo(), key, config);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		List<Tally> result = mysqlNodeManager.findTallyMulti(getTallyFieldInfo(), keys, config);
		return Scanner.of(result)
				.toMap(tally -> tally.getKey().getId(), Tally::getTally);
	}

	@Override
	public void deleteTally(String key, Config config){
		mysqlNodeManager.delete(getTallyFieldInfo(), new TallyKey(key), config);
	}

	private PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> getTallyFieldInfo(){
		return this.getFieldInfo();
	}

	@Override
	public void vacuum(Config config){
		mysqlNodeManager.vacuum(getTallyFieldInfo(), config);
	}
}
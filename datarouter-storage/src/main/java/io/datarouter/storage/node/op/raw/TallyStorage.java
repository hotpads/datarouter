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
package io.datarouter.storage.node.op.raw;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

public interface TallyStorage
extends NodeOps<TallyKey,Tally>{

	/*----------- op names --------------*/

	public static final String OP_incrementAndGetCount = "incrementAndGetCount";
	public static final String OP_findTallyCount = "findTallyCount";
	public static final String OP_getMultiTallyCount = "getMultiTallyCount";
	public static final String OP_deleteTally = "deleteTally";
	public static final String OP_vacuum = "vacuum";

	/*----------- increment --------------*/

	Long incrementAndGetCount(String key, int delta, Config config);

	default Long incrementAndGetCount(String key, int delta){
		return incrementAndGetCount(key, delta, new Config());
	}
	/*----------- find --------------*/

	Optional<Long> findTallyCount(String key, Config config);

	default Optional<Long> findTallyCount(String key){
		return findTallyCount(key, new Config());
	}

	/*----------- get --------------*/

	Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config);

	default Map<String,Long> getMultiTallyCount(Collection<String> keys){
		return getMultiTallyCount(keys, new Config());
	}

	/*----------- delete --------------*/

	void deleteTally(String key, Config config);

	default void deleteTally(String key){
		deleteTally(key, new Config());
	}

	void vacuum(Config config);

	/*------------- nodes -------------*/

	public interface TallyStorageNode
	extends TallyStorage, Node<TallyKey,Tally,TallyFielder>{
	}

	public interface PhysicalTallyStorageNode
	extends TallyStorageNode, PhysicalNode<TallyKey,Tally,TallyFielder>{
	}

}

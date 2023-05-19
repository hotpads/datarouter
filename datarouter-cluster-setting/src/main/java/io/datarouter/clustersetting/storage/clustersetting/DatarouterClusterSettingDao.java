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
package io.datarouter.clustersetting.storage.clustersetting;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting.ClusterSettingFielder;
import io.datarouter.model.databean.Databean;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.SettinglessDatabeanNodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterClusterSettingDao extends BaseDao{

	public static class DatarouterClusterSettingDaoParams extends BaseRedundantDaoParams{

	public DatarouterClusterSettingDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<ClusterSettingKey,ClusterSetting,ClusterSettingFielder> node;
	private final SingletonSupplier<AtomicReference<Map<ClusterSettingKey,ClusterSetting>>> cacheRefSupplier;

	@Inject
	public DatarouterClusterSettingDao(
			Datarouter datarouter,
			SettinglessDatabeanNodeFactory settinglessNodeFactory,
			DatarouterClusterSettingDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<ClusterSettingKey,ClusterSetting,ClusterSettingFielder> node =
							settinglessNodeFactory.create(clientId, ClusterSetting::new, ClusterSettingFielder::new)
							.withTag(Tag.DATAROUTER)
							// TODO remove?
							.withDisableForcePrimary(true)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
		cacheRefSupplier = SingletonSupplier.of(() -> new AtomicReference<>(loadCache()));
	}

	public Scanner<ClusterSetting> scan(){
		return node.scan();
	}

	public Scanner<ClusterSetting> scan(Range<ClusterSettingKey> range){
		return node.scan(range);
	}

	public Scanner<ClusterSetting> scanWithName(String name){
		return node.scanWithPrefix(ClusterSettingKey.prefix(name));
	}

	public Scanner<ClusterSetting> scanWithPrefix(ClusterSettingKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public Scanner<ClusterSettingKey> scanKeysWithPrefix(ClusterSettingKey prefix){
		return node.scanKeysWithPrefix(prefix);
	}

	public Scanner<ClusterSettingKey> scanKeysWithPrefixes(Collection<ClusterSettingKey> prefixes){
		return node.scanKeysWithPrefixes(prefixes);
	}

	public void put(ClusterSetting databean){
		node.put(databean);
	}

	public void putMulti(Collection<ClusterSetting> databeans){
		node.putMulti(databeans);
	}

	public ClusterSetting get(ClusterSettingKey key){
		return node.get(key);
	}

	public List<ClusterSetting> getMulti(Collection<ClusterSettingKey> keys){
		return node.getMulti(keys);
	}

	public void delete(ClusterSettingKey key){
		node.delete(key);
	}

	public void deleteMulti(Collection<ClusterSettingKey> keys){
		node.deleteMulti(keys);
	}

	public boolean exists(ClusterSettingKey key){
		return node.exists(key);
	}

	public Optional<ClusterSetting> find(ClusterSettingKey key){
		return node.find(key);
	}

	private Map<ClusterSettingKey,ClusterSetting> loadCache(){
		return scan().toMap(Databean::getKey);
	}

	public void refreshCache(){
		cacheRefSupplier.get().set(loadCache());
	}

	public List<ClusterSetting> getMultiFromCache(Collection<ClusterSettingKey> keys){
		Map<ClusterSettingKey,ClusterSetting> cache = cacheRefSupplier.get().get();
		return Scanner.of(keys)
				.map(cache::get)
				.exclude(Objects::isNull)
				.list();
	}

}

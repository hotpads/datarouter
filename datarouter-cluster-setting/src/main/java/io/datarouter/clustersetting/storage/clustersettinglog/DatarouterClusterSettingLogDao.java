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
package io.datarouter.clustersetting.storage.clustersettinglog;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog.ClusterSettingLogFielder;
import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.IndexingNodeFactory;
import io.datarouter.storage.node.factory.SettinglessDatabeanNodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.index.IndexReader;
import io.datarouter.storage.tag.Tag;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.types.MilliTimeReversed;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantIndexedSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterClusterSettingLogDao extends BaseDao{

	public record DatarouterClusterSettingLogDaoParams(List<ClientId> clientIds){
	}

	private final IndexedSortedMapStorageNode<ClusterSettingLogKey,ClusterSettingLog,ClusterSettingLogFielder>
			node;
	private final IndexReader<
			ClusterSettingLogKey,
			ClusterSettingLog,
			ClusterSettingLogByReversedCreatedMsKey,
			FieldlessIndexEntry<ClusterSettingLogByReversedCreatedMsKey,ClusterSettingLogKey,ClusterSettingLog>>
			byReversedCreatedMs;

	@Inject
	public DatarouterClusterSettingLogDao(
			Datarouter datarouter,
			SettinglessDatabeanNodeFactory settinglessNodeFactory,
			DatarouterClusterSettingLogDaoParams params,
			IndexingNodeFactory indexingNodeFactory){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					IndexedSortedMapStorageNode<ClusterSettingLogKey,ClusterSettingLog,ClusterSettingLogFielder> node =
							settinglessNodeFactory.create(
									clientId,
									ClusterSettingLog::new,
									ClusterSettingLogFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
					})
				.listTo(RedundantIndexedSortedMapStorageNode::makeIfMulti);
		byReversedCreatedMs = indexingNodeFactory.createKeyOnlyManagedIndex(
				ClusterSettingLogByReversedCreatedMsKey::new, node)
				.build();
		datarouter.register(node);
	}

	public Optional<ClusterSettingLog> find(ClusterSettingLogKey key){
		return node.find(key);
	}

	public void put(ClusterSettingLog databean){
		node.put(databean);
	}

	public void putMulti(Collection<ClusterSettingLog> databeans){
		node.putMulti(databeans);
	}

	public boolean hasLogs(String settingName){
		var prefix = ClusterSettingLogKey.prefix(settingName);
		return node.scanWithPrefix(prefix, new Config().setLimit(1))
				.hasAny();
	}

	public long countSettingLogs(String settingName){
		var prefix = ClusterSettingLogKey.prefix(settingName);
		return node.scanWithPrefix(prefix)
				.count();
	}

	public Scanner<ClusterSettingLog> scanWithWildcardPrefix(String settingNamePrefix){
		Range<ClusterSettingLogKey> range = KeyRangeTool.forPrefixWithWildcard(
				settingNamePrefix,
				ClusterSettingLogKey::prefix);
		return node.scan(range);
	}

	public Scanner<ClusterSettingLog> scanWithPrefix(ClusterSettingLogKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public Scanner<ClusterSettingLog> scanWithPrefixes(Collection<ClusterSettingLogKey> prefixes){
		return node.scanWithPrefixes(prefixes);
	}

	public Scanner<ClusterSettingLog> scanByReversedCreatedMs(
			Range<ClusterSettingLogByReversedCreatedMsKey>range){
		return byReversedCreatedMs.scanDatabeans(range);
	}

	public Scanner<ClusterSettingLog> scanBeforeDesc(Instant instant){
		var timeReversed = MilliTimeReversed.of(instant);
		var prefix = ClusterSettingLogByReversedCreatedMsKey.prefix(timeReversed);
		var range = new Range<>(prefix, false, null, true);
		return scanByReversedCreatedMs(range);
	}

	public boolean isOldDatabaseSetting(ClusterSetting databeanSetting, int numOfDays){
		Instant maxSettingAge = Instant.now().minus(Duration.ofDays(numOfDays));
		return node
				.scanKeysWithPrefix(ClusterSettingLogKey.prefix(databeanSetting.getName()),
						new Config().setLimit(1))
				.findFirst()
				.map(ClusterSettingLogKey::getMilliTimeReversed)
				.map(MilliTimeReversed::toInstant)
				.map(created -> created.isBefore(maxSettingAge))
				.orElse(false);
	}

}

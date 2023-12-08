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
package io.datarouter.loggerconfig.storage.loggerconfig;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.datarouter.loggerconfig.LoggingConfig;
import io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig.LoggerConfigFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.types.MilliTime;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterLoggerConfigDao extends BaseDao{

	public record DatarouterLoggerConfigDaoParams(List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<LoggerConfigKey,LoggerConfig,LoggerConfigFielder> node;

	@Inject
	public DatarouterLoggerConfigDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterLoggerConfigDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<LoggerConfigKey,LoggerConfig,LoggerConfigFielder> node =
							nodeFactory.create(clientId, LoggerConfig::new, LoggerConfigFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public Scanner<LoggerConfig> scan(){
		return node.scan();
	}

	public void saveLoggerConfig(
			String name,
			LoggingLevel level,
			boolean additive,
			List<String> appendersRef,
			String email,
			Long ttlMillis){
		var loggerConfig = new LoggerConfig(name, level, additive, appendersRef, email, MilliTime.now(), ttlMillis);
		node.put(loggerConfig);
	}

	public void delete(LoggerConfigKey key){
		node.delete(key);
	}

	public void deleteLoggerConfig(String name){
		node.delete(new LoggerConfigKey(name));
	}

	public void deleteLoggerConfigs(Collection<LoggerConfig> loggerConfigs){
		Scanner.of(loggerConfigs)
				.map(LoggerConfig::getKey)
				.flush(node::deleteMulti);
	}

	public String getLoggingLevelFromConfigName(String name){
		return node.find(new LoggerConfigKey(name))
				.map(io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig::getLevel)
				.map(LoggingLevel::getPersistentString)
				.orElse("?");
	}

	public Map<String,LoggerConfig> getLoggerConfigs(List<String> names){
		return Scanner.of(names)
				.map(LoggerConfigKey::new)
				.listTo(node::scanMulti)
				.toMap(config -> config.getKey().getName());
	}

	public Collection<LoggerConfig> expireLoggerConfigs(LoggingConfig config){
		Set<LoggerConfig> expiredLoggerConfigs = config.getLoggerConfigs().stream()
				.filter(DatarouterLoggerConfigDao::shouldExpire)
				.collect(Collectors.toSet());
		if(expiredLoggerConfigs.isEmpty()){
			return List.of();
		}
		// TODO don't edit the passed-in List
		config.getLoggerConfigs().removeIf(expiredLoggerConfigs::contains);
		deleteLoggerConfigs(expiredLoggerConfigs);
		return expiredLoggerConfigs;
	}

	private static boolean shouldExpire(LoggerConfig loggerConfig){
		Long ttl = loggerConfig.getTtlMillis();
		if(ttl == null || ttl == 0){
			return false;
		}
		return MilliTime.now().isAfter(loggerConfig.getLastUpdated().plus(ttl, ChronoUnit.MILLIS));
	}

}

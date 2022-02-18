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
package io.datarouter.storage.client;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

public interface ClientOptionsFactory{

	Properties getInternalConfigDirectoryTypeOptions(String internalConfigDirectoryTypeName);

	default List<ClientId> getRequiredClientIds(){
		return List.of();
	}

	default Properties mergeOptions(Properties... options){
		var merged = new Properties();
		Scanner.of(options)
				.each(props -> props.keySet()
						.forEach(key -> Require.isFalse(merged.containsKey(key), "duplicate key " + key)))
				.forEach(merged::putAll);
		return merged;
	}

	default Properties mergeBuilders(Collection<ClientOptionsBuilder> builders){
		Properties[] multiProperties = builders.stream()
				.map(ClientOptionsBuilder::build)
				.toArray(Properties[]::new);
		return mergeOptions(multiProperties);
	}

	default Properties mergeBuilders(ClientOptionsBuilder... builders){
		return mergeBuilders(List.of(builders));
	}

	default Properties mergeBuilders(
			Collection<ClientOptionsBuilder> builderCollection,
			ClientOptionsBuilder... builderVarargs){
		return Scanner.of(builderCollection, List.of(builderVarargs))
				.concat(Scanner::of)
				.listTo(this::mergeBuilders);
	}

	@Singleton
	class NoOpClientOptionsFactory implements ClientOptionsFactory{

		@Override
		public Properties getInternalConfigDirectoryTypeOptions(String internalConfigDirectoryTypeName){
			return new Properties();
		}

	}

}

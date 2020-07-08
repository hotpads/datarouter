/**
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;

public interface ClientOptionsFactory{

	Properties getInternalConfigDirectoryTypeOptions(String internalConfigDirectoryTypeName);

	default Properties mergeOptions(Properties... options){
		return Arrays.stream(options)
				.collect(Properties::new, Map::putAll, Map::putAll);
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

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
package io.datarouter.plugin.copytable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import io.datarouter.storage.config.Config;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.types.optional.OptionalString;

public interface CopyTableConfiguration{

	List<String> getFilterNames();

	Optional<Predicate<?>> findFilter(String name);

	default void assertValidFilter(String name){
		Require.isPresent(findFilter(name), "invalid filter " + name);
	}

	default Predicate<?> getValidFilter(OptionalString filterName){
		String name = filterName
				.map(StringTool::nullIfEmpty)
				.orElse(null);
		if(name == null){
			return null;
		}
		assertValidFilter(name);
		return findFilter(name).get();
	}

	Optional<BiConsumer<?,Config>> findProcessor(String processorName);

	static class NoOpCopyTableConfiguration implements CopyTableConfiguration{

		@Override
		public List<String> getFilterNames(){
			return List.of();
		}

		@Override
		public Optional<Predicate<?>> findFilter(String name){
			return Optional.empty();
		}

		@Override
		public void assertValidFilter(String name){
			// noop
		}

		@Override
		public Optional<BiConsumer<?,Config>> findProcessor(String processorName){
			return Optional.empty();
		}

	}

}

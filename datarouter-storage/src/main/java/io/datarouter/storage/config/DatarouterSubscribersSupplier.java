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
package io.datarouter.storage.config;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Singleton;

// Set of email aliases to receive all datarouter emails
public interface DatarouterSubscribersSupplier extends Supplier<Set<String>>{

	default String getAsCsv(){
		return String.join(",", get());
	}

	@Singleton
	class NoOpDatarouterSubscribers implements DatarouterSubscribersSupplier{

		@Override
		public Set<String> get(){
			return Collections.emptySet();
		}

	}

	@Singleton
	class DatarouterSubscribers implements DatarouterSubscribersSupplier{

		private final Set<String> subscribers;

		public DatarouterSubscribers(Set<String> subscribers){
			this.subscribers = subscribers;
		}

		@Override
		public Set<String> get(){
			return subscribers;
		}

	}

}

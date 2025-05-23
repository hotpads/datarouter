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
package io.datarouter.relay;

import java.util.List;

import jakarta.inject.Singleton;

public interface DatarouterRelayTopics{

	List<String> permissionRequest();
	List<String> permissionChanged();
	List<String> dailyDigestSummary();
	List<String> dailyDigestActionable();
	List<String> dailyDigestStaleTables();
	List<String> schemaUpdateServiceStartup();

	@Singleton
	class DefaultDatarouterRelayTopics implements DatarouterRelayTopics{

		@Override
		public List<String> permissionRequest(){
			return List.of();
		}

		@Override
		public List<String> permissionChanged(){
			return List.of();
		}

		@Override
		public List<String> dailyDigestSummary(){
			return List.of();
		}

		@Override
		public List<String> dailyDigestActionable(){
			return List.of();
		}

		@Override
		public List<String> dailyDigestStaleTables(){
			return List.of();
		}

		@Override
		public List<String> schemaUpdateServiceStartup(){
			return List.of();
		}

	}

}

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
package io.datarouter.instrumentation.changelog;

import java.util.Optional;

public interface ChangelogRecorder{

	default void record(String changelogType, String name, String action, String username){
		record(changelogType, name, action, username, null);
	}

	default void record(String changelogType, String name, String action, String username, String comment){
		record(changelogType, name, action, username, comment);
	}

	void recordAndSendEmail(
			String changelogType,
			String name,
			String action,
			String username,
			Optional<String> comment,
			Optional<String> additionalSendTos);

	class NoOpChangelogRecorder implements ChangelogRecorder{

		@Override
		public void record(String changelogType, String name, String action, String username, String comment){
		}

		@Override
		public void recordAndSendEmail(
				String changelogType,
				String name,
				String action,
				String username,
				Optional<String> comment,
				Optional<String> additionalSendTos){
		}

	}

}

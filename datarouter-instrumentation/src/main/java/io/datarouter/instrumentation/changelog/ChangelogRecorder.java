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
package io.datarouter.instrumentation.changelog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ChangelogRecorder{

	void record(DatarouterChangelogDto changelogDto);
	void update(ChangelogDto changelogDto);

	class NoOpChangelogRecorder implements ChangelogRecorder{

		@Override
		public void record(DatarouterChangelogDto changelogDto){
		}

		@Override
		public void update(ChangelogDto changelogDto){
		}

	}

	public static class DatarouterChangelogDtoBuilder{

		public final String changelogType;
		public final String name;
		public final String action;
		public final String username;

		public Optional<String> comment = Optional.empty();
		public Optional<String> note = Optional.empty();
		public List<String> additionalSendTos = new ArrayList<>();
		public boolean sendEmail = false;
		public boolean includeMainDatarouterAdmin = true;
		public boolean includeSubscribers = true;

		public DatarouterChangelogDtoBuilder(String changelogType, String name, String action, String username){
			this.changelogType = changelogType;
			this.name = name;
			this.action = action;
			this.username = username;
		}

		public DatarouterChangelogDtoBuilder sendEmail(){
			this.sendEmail = true;
			return this;
		}

		public DatarouterChangelogDtoBuilder withComment(String comment){
			this.comment = Optional.of(comment);
			return this;
		}

		public DatarouterChangelogDtoBuilder withNote(String note){
			this.note = Optional.of(note);
			return this;
		}

		public DatarouterChangelogDtoBuilder excludeMainDatarouterAdmin(){
			includeMainDatarouterAdmin = false;
			return this;
		}

		public DatarouterChangelogDtoBuilder excludeSubscribers(){
			includeSubscribers = false;
			return this;
		}

		@Deprecated
		public DatarouterChangelogDtoBuilder excludeAdditionalAdministrators(){
			return excludeSubscribers();
		}

		public DatarouterChangelogDtoBuilder additionalSendTos(String additionalSendTo){
			additionalSendTos.add(additionalSendTo);
			return this;
		}

		public DatarouterChangelogDto build(){
			return new DatarouterChangelogDto(
					changelogType,
					name,
					action,
					username,
					comment,
					note,
					additionalSendTos,
					sendEmail,
					includeMainDatarouterAdmin,
					includeSubscribers);
		}

	}

	// keep separate from ChangelogDto
	public static class DatarouterChangelogDto{

		public final String changelogType;
		public final String name;
		public final String action;
		public final String username;

		public final Optional<String> comment;
		public final Optional<String> note;
		public final List<String> additionalSendTos;
		public final boolean sendEmail;
		public final boolean includeMainDatarouterAdmin;
		public final boolean includeSubscribers;

		private DatarouterChangelogDto(
				String changelogType,
				String name,
				String action,
				String username,
				Optional<String> comment,
				Optional<String> note,
				List<String> additionalSendTos,
				boolean sendEmail,
				boolean includeMainDatarouterAdmin,
				boolean includeSubscribers){
			this.changelogType = changelogType;
			this.name = name;
			this.action = action;
			this.username = username;
			this.comment = comment;
			this.note = note;
			this.additionalSendTos = additionalSendTos;
			this.sendEmail = sendEmail;
			this.includeMainDatarouterAdmin = includeMainDatarouterAdmin;
			this.includeSubscribers = includeSubscribers;
		}

	}

}

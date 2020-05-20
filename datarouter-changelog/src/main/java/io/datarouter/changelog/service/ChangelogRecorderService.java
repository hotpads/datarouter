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
package io.datarouter.changelog.service;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.changelog.config.DatarouterChangelogSettingRoot;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.changelog.ChangelogDto;
import io.datarouter.instrumentation.changelog.ChangelogPublisher;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;

@Singleton
public class ChangelogRecorderService implements ChangelogRecorder{

	@Inject
	private ChangelogPublisher publisher;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterChangelogSettingRoot settings;
	@Inject
	private ChangelogDao dao;

	@Override
	public void record(String changelogType, String name, String action, String username, String comment){
		var dto = new ChangelogDto(
				datarouterService.getName(),
				changelogType,
				name,
				new Date().getTime(),
				action,
				username,
				comment);
		if(settings.publishChangelog.get()){
			publisher.add(dto);
		}
		dao.put(new Changelog(dto));
	}

}

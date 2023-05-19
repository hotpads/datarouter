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
package io.datarouter.clustersetting.service;

import java.time.ZoneId;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingLogAction;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;

@Singleton
public class ClusterSettingChangeListener{

	@Inject
	private DatarouterClusterSettingLogDao logDao;
	@Inject
	private ClusterSettingChangelogService changelogService;
	@Inject
	private ClusterSettingEmailService emailService;

	public void onCreate(
			ClusterSetting newSetting,
			String username,
			Optional<String> optComment,
			ZoneId zoneId){

		// ClusterSettingLog
		var clusterSettingLog = new ClusterSettingLog(
				newSetting,
				ClusterSettingLogAction.INSERTED,
				username,
				optComment.orElse(null));
		logDao.put(clusterSettingLog);

		// Changelog
		changelogService.recordChangelog(
				clusterSettingLog.getKey().getName(),
				ClusterSettingLogAction.INSERTED.persistentString,
				username,
				optComment);

		// Email
		emailService.sendEmail(
				clusterSettingLog,
				null,
				Optional.of(username),
				zoneId);
	}

	public void onUpdateOrDelete(
			ClusterSetting oldSetting,
			ClusterSettingLogAction action,
			String username,
			Optional<String> optComment,
			ZoneId zoneId){

		// ClusterSettingLog
		var clusterSettingLog = new ClusterSettingLog(
				oldSetting,
				action,
				username,
				optComment.orElse(null));
		logDao.put(clusterSettingLog);

		// Changelog
		changelogService.recordChangelog(
				clusterSettingLog.getKey().getName(),
				action.persistentString,
				username,
				optComment);

		// Email
		emailService.sendEmail(
				clusterSettingLog,
				oldSetting.getValue(),
				Optional.of(username),
				zoneId);
	}

}

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
package io.datarouter.email.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.email.type.DatarouterEmailTypes.AvailabilitySwitchEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.AwsRdsEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.ClusterSettingEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.DailyDigestEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.LoggerConfigCleanupEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.LongRunningTaskFailureAlertEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.LongRunningTaskTrackerEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.NodewatchEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.WebappInstanceAlertEmailType;
import io.datarouter.storage.config.BaseStoragePlugin;

public class DatarouterEmailPlugin extends BaseStoragePlugin{

	private final List<String> emailRecipientsClusterSettingUpdate;
	private final List<String> emailRecipientsPermissionRequests;
	private final List<String> emailRecipientsNodewatch;
	private final List<String> emailRecipientsWebappInstanceAlert;
	private final List<String> emailRecipientsLongRunningTaskFailureAlert;
	private final List<String> emailRecipientsLongRunningTaskTracker;
	private final List<String> emailRecipientsLoggerConfigCleanup;
	private final List<String> emailRecipientsAvailabilitySwitch;
	private final List<String> emailRecipientsAwsRds;
	private final List<String> emailRecipientsDailyDigest;

	private DatarouterEmailPlugin(
			List<String> emailRecipientsClusterSettingUpdate,
			List<String> emailRecipientsPermissionRequests,
			List<String> emailRecipientsNodewatch,
			List<String> emailRecipientsWebappInstanceAlert,
			List<String> emailRecipientsLongRunningTaskFailureAlert,
			List<String> emailRecipientsLongRunningTaskTracker,
			List<String> emailRecipientsLoggerConfigCleanup,
			List<String> emailRecipientsAvailabilitySwitch,
			List<String> emailRecipientsAwsRds,
			List<String> emailRecipientsDailyDigest){
		addSettingRoot(DatarouterEmailSettingRoot.class);

		this.emailRecipientsClusterSettingUpdate = emailRecipientsClusterSettingUpdate;
		this.emailRecipientsPermissionRequests = emailRecipientsPermissionRequests;
		this.emailRecipientsNodewatch = emailRecipientsNodewatch;
		this.emailRecipientsWebappInstanceAlert = emailRecipientsWebappInstanceAlert;
		this.emailRecipientsLongRunningTaskFailureAlert = emailRecipientsLongRunningTaskFailureAlert;
		this.emailRecipientsLongRunningTaskTracker = emailRecipientsLongRunningTaskTracker;
		this.emailRecipientsLoggerConfigCleanup = emailRecipientsLoggerConfigCleanup;
		this.emailRecipientsAvailabilitySwitch = emailRecipientsAvailabilitySwitch;
		this.emailRecipientsAwsRds = emailRecipientsAwsRds;
		this.emailRecipientsDailyDigest = emailRecipientsDailyDigest;
	}

	@Override
	public String getName(){
		return "DatarouterEmail";
	}

	@Override
	protected void configure(){
		bind(ClusterSettingEmailType.class)
				.toInstance(new ClusterSettingEmailType(emailRecipientsClusterSettingUpdate));
		bind(PermissionRequestEmailType.class)
				.toInstance(new PermissionRequestEmailType(emailRecipientsPermissionRequests));
		bind(NodewatchEmailType.class)
				.toInstance(new NodewatchEmailType(emailRecipientsNodewatch));
		bind(WebappInstanceAlertEmailType.class)
				.toInstance(new WebappInstanceAlertEmailType(emailRecipientsWebappInstanceAlert));
		bind(LongRunningTaskFailureAlertEmailType.class)
				.toInstance(new LongRunningTaskFailureAlertEmailType(emailRecipientsLongRunningTaskFailureAlert));
		bind(LongRunningTaskTrackerEmailType.class)
				.toInstance(new LongRunningTaskTrackerEmailType(emailRecipientsLongRunningTaskTracker));
		bind(LoggerConfigCleanupEmailType.class)
				.toInstance(new LoggerConfigCleanupEmailType(emailRecipientsLoggerConfigCleanup));
		bind(AvailabilitySwitchEmailType.class)
				.toInstance(new AvailabilitySwitchEmailType(emailRecipientsAvailabilitySwitch));
		bind(AwsRdsEmailType.class)
				.toInstance(new AwsRdsEmailType(emailRecipientsAwsRds));
		bind(DailyDigestEmailType.class)
				.toInstance(new DailyDigestEmailType(emailRecipientsDailyDigest));
	}

	public static class DatarouterEmailPluginBuilder{

		private final List<String> emailRecipientsClusterSettingUpdate = new ArrayList<>();
		private final List<String> emailRecipientsPermissionRequests = new ArrayList<>();
		private final List<String> emailRecipientsNodewatch = new ArrayList<>();
		private final List<String> emailRecipientsWebappInstanceAlert = new ArrayList<>();
		private final List<String> emailRecipientsLongRunningTaskFailureAlert = new ArrayList<>();
		private final List<String> emailRecipientsLongRunningTaskTracker = new ArrayList<>();
		private final List<String> emailRecipientsLoggerConfigCleanup = new ArrayList<>();
		private final List<String> emailRecipientsAvailabilitySwitch = new ArrayList<>();
		private final List<String> emailRecipientsAwsRds = new ArrayList<>();
		private final List<String> emailRecipientsDailyDigest = new ArrayList<>();

		public DatarouterEmailPluginBuilder addClusterSettingEmailRecipients(List<String> tos){
			emailRecipientsClusterSettingUpdate.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addPermissionRequestEmailRecipients(List<String> tos){
			emailRecipientsPermissionRequests.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addNodewatchEmailRecipients(List<String> tos){
			emailRecipientsNodewatch.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addWebappInstanceAlertEmailRecipients(List<String> tos){
			emailRecipientsWebappInstanceAlert.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addLongRunningTaskFailureAlertEmailRecipients(List<String> tos){
			emailRecipientsLongRunningTaskFailureAlert.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addLongRunningTaskTrackerEmailRecipients(List<String> tos){
			emailRecipientsLongRunningTaskTracker.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addLoggerConfigCleanupEmailRecipients(List<String> tos){
			emailRecipientsLoggerConfigCleanup.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addAvailabilitySwitchEmailRecipients(List<String> tos){
			emailRecipientsAvailabilitySwitch.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addAwsRdsEmailRecipients(List<String> tos){
			emailRecipientsAwsRds.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addDailyDigestEmailRecipients(List<String> tos){
			emailRecipientsDailyDigest.addAll(tos);
			return this;
		}

		public DatarouterEmailPlugin build(){
			return new DatarouterEmailPlugin(
					emailRecipientsClusterSettingUpdate,
					emailRecipientsPermissionRequests,
					emailRecipientsNodewatch,
					emailRecipientsWebappInstanceAlert,
					emailRecipientsLongRunningTaskFailureAlert,
					emailRecipientsLongRunningTaskTracker,
					emailRecipientsLoggerConfigCleanup,
					emailRecipientsAvailabilitySwitch,
					emailRecipientsAwsRds,
					emailRecipientsDailyDigest);
		}

	}

}

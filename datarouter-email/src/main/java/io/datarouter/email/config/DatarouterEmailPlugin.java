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

import io.datarouter.email.type.DatarouterEmailTypes.ClusterSettingEmailType;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.storage.config.BaseStoragePlugin;

public class DatarouterEmailPlugin extends BaseStoragePlugin{

	private final List<String> emailRecipientsClusterSettingUpdate;
	private final List<String> emailRecipientsPermissionRequests;

	private DatarouterEmailPlugin(
			List<String> emailRecipientsClusterSettingUpdate,
			List<String> emailRecipientsPermissionRequests){
		addSettingRoot(DatarouterEmailSettingRoot.class);
		this.emailRecipientsClusterSettingUpdate = emailRecipientsClusterSettingUpdate;
		this.emailRecipientsPermissionRequests = emailRecipientsPermissionRequests;
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
	}

	public static class DatarouterEmailPluginBuilder{

		private final List<String> emailRecipientsClusterSettingUpdate = new ArrayList<>();
		private final List<String> emailRecipientsPermissionRequests = new ArrayList<>();

		public DatarouterEmailPluginBuilder addClusterSettingEmailRecipients(List<String> tos){
			emailRecipientsClusterSettingUpdate.addAll(tos);
			return this;
		}

		public DatarouterEmailPluginBuilder addPermissionRequestEmailRecipients(List<String> tos){
			emailRecipientsPermissionRequests.addAll(tos);
			return this;
		}

		public DatarouterEmailPlugin build(){
			return new DatarouterEmailPlugin(
					emailRecipientsClusterSettingUpdate,
					emailRecipientsPermissionRequests);
		}

	}

}

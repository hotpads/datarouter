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
package io.datarouter.email.type;

import java.util.List;

/*
 * TODO
 * - changelog (count keys)
 * - configuration scan (job)
 */
public class DatarouterEmailTypes{

	public static class CountKeysEmailType extends SimpleEmailType{
		public CountKeysEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class ClusterSettingEmailType extends SimpleEmailType{
		public ClusterSettingEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class PermissionRequestEmailType extends SimpleEmailType{
		public PermissionRequestEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class NodewatchEmailType extends SimpleEmailType{
		public NodewatchEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class WebappInstanceAlertEmailType extends SimpleEmailType{
		public WebappInstanceAlertEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class LongRunningTaskFailureAlertEmailType extends SimpleEmailType{
		public LongRunningTaskFailureAlertEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class LongRunningTaskTrackerEmailType extends SimpleEmailType{
		public LongRunningTaskTrackerEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class LoggerConfigCleanupEmailType extends SimpleEmailType{
		public LoggerConfigCleanupEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class AvailabilitySwitchEmailType extends SimpleEmailType{
		public AvailabilitySwitchEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class AwsRdsEmailType extends SimpleEmailType{
		public AwsRdsEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class DailyDigestActionableEmailType extends SimpleEmailType{
		public DailyDigestActionableEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class DailyDigestSummaryEmailType extends SimpleEmailType{
		public DailyDigestSummaryEmailType(List<String> tos){
			super(tos);
		}
	}

	public static class SchemaUpdatesEmailType extends SimpleEmailType{
		public SchemaUpdatesEmailType(List<String> tos){
			super(tos);
		}
	}

}

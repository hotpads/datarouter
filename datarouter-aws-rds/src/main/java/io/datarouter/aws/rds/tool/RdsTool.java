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
package io.datarouter.aws.rds.tool;

public class RdsTool{

	public static final String USER_EVENTS_LINK = "https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/"
			+ "USER_Events.Messages.html#USER_Events.Messages.";

	public static String buildClusterMonitoringLink(String region, String clusterName){
		return new StringBuilder("https://")
				.append(region)
				.append(".console.aws.amazon.com/rds/home?region=")
				.append(region)
				.append("#database:id=")
				.append(clusterName)
				.append(";is-cluster=true;tab=monitoring")
				.toString();
	}

}

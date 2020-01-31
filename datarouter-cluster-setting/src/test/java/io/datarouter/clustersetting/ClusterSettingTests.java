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
package io.datarouter.clustersetting;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSettingKey;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

public class ClusterSettingTests{

	@Test
	public void testAppliesToWebappInstance(){
		String application = "myApplication";
		String serverName = "myServerName";
		String serverType = "myServerType";
		WebappInstance app = new WebappInstance(application, serverName, serverType);

		ClusterSettingKey keyDefault = new ClusterSettingKey("a", ClusterSettingScope.DEFAULT_SCOPE, null, null, null);
		Assert.assertTrue(keyDefault.appliesToWebappInstance(app));

		ClusterSettingKey keyCluster = new ClusterSettingKey("b", ClusterSettingScope.CLUSTER, null, null, null);
		Assert.assertTrue(keyCluster.appliesToWebappInstance(app));

		ClusterSettingKey keyServerType = new ClusterSettingKey("c", ClusterSettingScope.SERVER_TYPE, serverType, null,
				null);
		Assert.assertTrue(keyServerType.appliesToWebappInstance(app));

		ClusterSettingKey keyServerName = new ClusterSettingKey("d", ClusterSettingScope.SERVER_NAME, null, serverName,
				null);
		Assert.assertTrue(keyServerName.appliesToWebappInstance(app));

		ClusterSettingKey keyApplication = new ClusterSettingKey("e", ClusterSettingScope.APPLICATION, null, null,
				application);
		Assert.assertTrue(keyApplication.appliesToWebappInstance(app));

		ClusterSettingKey keyApplicationFalse = new ClusterSettingKey("eFalse", ClusterSettingScope.APPLICATION, null,
				null, "not-" + application);
		Assert.assertFalse(keyApplicationFalse.appliesToWebappInstance(app));
	}

}

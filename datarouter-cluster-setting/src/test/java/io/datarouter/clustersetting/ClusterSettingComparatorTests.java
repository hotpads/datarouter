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
package io.datarouter.clustersetting;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.clustersetting.enums.ClusterSettingScope;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.util.ClusterSettingComparisonTool;
import io.datarouter.storage.servertype.ServerType;

public class ClusterSettingComparatorTests{

	@Test
	public void testClusterSettingComparator(){
		List<ClusterSetting> settings = new ArrayList<>();
		var defaultScopedSetting = new ClusterSetting(
				"defaultScopedSetting",
				ClusterSettingScope.DEFAULT_SCOPE,
				ServerType.UNKNOWN.getPersistentString(),
				"",
				"");
		settings.add(defaultScopedSetting);
		var serverTypeScopedSetting = new ClusterSetting(
				"serverTypeScopedSetting",
				ClusterSettingScope.SERVER_TYPE,
				ServerType.DEV.getPersistentString(),
				"",
				"");
		settings.add(serverTypeScopedSetting);
		Assert.assertEquals(ClusterSettingComparisonTool.getMostSpecificSetting(settings).orElse(null),
				serverTypeScopedSetting);
		var serverNameScopedSetting = new ClusterSetting(
				"serverNameScopedSetting",
				ClusterSettingScope.SERVER_NAME,
				ServerType.UNKNOWN.getPersistentString(),
				"mySevrer",
				"");
		settings.add(serverNameScopedSetting);
		Assert.assertEquals(ClusterSettingComparisonTool.getMostSpecificSetting(settings).orElse(null),
				serverNameScopedSetting);
	}

}

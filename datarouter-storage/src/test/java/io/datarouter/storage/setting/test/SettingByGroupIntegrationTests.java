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
package io.datarouter.storage.setting.test;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.storage.setting.SettingByGroup;
import io.datarouter.storage.test.DatarouterStorageTestModuleFactory;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public class SettingByGroupIntegrationTests{

	@Inject
	private TestSettingNode testSettingNode;

	private SettingByGroup<Integer> settingByGroup;

	@BeforeClass
	public void beforeClass(){
		settingByGroup = new SettingByGroup<>("setting1", 60, testSettingNode::registerInteger);
		settingByGroup.addSetting("group1", 61);
		settingByGroup.addSetting("group2", 62);
	}

	@Test
	public void testGetSetting(){
		int group1Setting = settingByGroup.getSetting("group1").getValue();
		Assert.assertEquals(group1Setting, 61);
		int group2Setting = settingByGroup.getSetting("group2").getValue();
		Assert.assertEquals(group2Setting, 62);
		int unknownGroupSetting = settingByGroup.getSetting("unknownGroup").getValue();
		Assert.assertEquals(unknownGroupSetting, 60);
	}

	@Test
	public void testGetDefaultSetting(){
		int defaultSetting = settingByGroup.getDefaultSetting().getValue();
		Assert.assertEquals(defaultSetting, 60);
	}
}
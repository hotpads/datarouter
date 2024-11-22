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
package io.datarouter.storage.setting;

import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.storage.setting.SettingRoot.SettingRootFinder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SettingBootstrapIntegrationService implements TestableService{

	@Inject
	private DatarouterInjector datarouterInjector;
	@Inject
	private SettingRootsSupplier settingRootsSupplier;
	@Inject
	private SettingRootFinder settingRootFinder;

	@Override
	public void testAll(){
		String orphanSettings = datarouterInjector.scanValuesOfType(SettingNode.class)
				// special case
				.exclude(node -> node == settingRootFinder)
				.exclude(node -> isInTree(node, settingRootsSupplier.get()))
				.map(SettingNode::getClass)
				.map(Class::getName)
				.collect(Collectors.joining(", "));
		if(!orphanSettings.isEmpty()){
			throw new RuntimeException("some setting nodes are not registered: " + orphanSettings);
		}
	}

	private boolean isInTree(SettingNode needle, List<? extends SettingNode> haystack){
		boolean presentAtThisLevel = haystack.stream().anyMatch(node -> needle == node);
		if(presentAtThisLevel){
			return true;
		}
		return haystack.stream()
				.map(SettingNode::getListChildren)
				.anyMatch(leaves -> isInTree(needle, leaves));
	}

}

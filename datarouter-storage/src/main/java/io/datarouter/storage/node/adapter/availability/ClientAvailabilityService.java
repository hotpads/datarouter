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
package io.datarouter.storage.node.adapter.availability;

import java.util.Collection;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.setting.impl.DatarouterClientAvailabilitySettings.AvailabilitySettingNode;
import io.datarouter.storage.config.setting.impl.DatarouterClientAvailabilitySettingsProvider;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.setting.Setting;

@Singleton
public class ClientAvailabilityService{

	@Inject
	private DatarouterClientAvailabilitySettingsProvider availabilitySettingsFactory;

	public boolean canWriteToAllPhysicalNodes(Node<?,?,?> node){
		return getAvailablitySettingNode(node)
				.map(availability -> availability.write)
				.allMatch(Setting::get);
	}

	public boolean canReadFromAllPhysicalNodes(Node<?,?,?> node){
		return getAvailablitySettingNode(node)
				.map(availability -> availability.read)
				.allMatch(Setting::get);
	}

	private Stream<AvailabilitySettingNode> getAvailablitySettingNode(Node<?,?,?> node){
		return node.getPhysicalNodes().stream()
				.map(PhysicalNode::getClientIds)
				.flatMap(Collection::stream)
				.distinct()
				.map(availabilitySettingsFactory.get()::getAvailabilityForClientId);
	}

}

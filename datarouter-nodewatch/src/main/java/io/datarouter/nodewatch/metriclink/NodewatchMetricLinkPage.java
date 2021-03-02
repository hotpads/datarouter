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
package io.datarouter.nodewatch.metriclink;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.metriclinks.MetricLinkDto;
import io.datarouter.web.metriclinks.MetricLinkDto.LinkDto;
import io.datarouter.web.metriclinks.MetricLinkPage;

@Singleton
public abstract class NodewatchMetricLinkPage implements MetricLinkPage{

	@Inject
	private DatarouterClients clients;
	@Inject
	private DatarouterNodes datarouterNodes;

	@Override
	public String getName(){
		return "Tables";
	}

	protected List<MetricLinkDto> buildMetricLinks(boolean isSystem){
		return Scanner.of(clients.getClientIds())
				.include(ClientId::getWritable)
				.map(ClientId::getName)
				.map(datarouterNodes::getPhysicalNodesForClient)
				.concat(Scanner::of)
				.map(PhysicalNode::getFieldInfo)
				.include(fieldInfo -> {
					if(isSystem){
						return fieldInfo.getIsSystemTable();
					}
					return !fieldInfo.getIsSystemTable();
				})
				.map(fieldInfo -> {
					String prefix = "Datarouter node "
							+ clients.getClientTypeInstance(fieldInfo.getClientId()).getName()
							+ " "
							+ fieldInfo.getClientId().getName()
							+ " "
							+ fieldInfo.getNodeName();
					var linkDto = LinkDto.of(prefix);
					return new MetricLinkDto(fieldInfo.getNodeName(), Optional.empty(), Optional.of(linkDto));
				})
				.list();
	}

}

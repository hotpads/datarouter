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
package io.datarouter.storage.node.tableconfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.dao.Daos;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

@Singleton
public class TableConfigurationService{

	@SuppressWarnings("unused") // used to init all daos
	@Inject
	private Daos daos;
	@Inject
	private DatarouterNodes datarouterNodes;

	public List<NodewatchConfiguration> getTableConfigurations(){
		return Scanner.of(datarouterNodes.getAllNodes())
				.map(Node::getPhysicalNodes)
				.concat(Scanner::of)
				.map(PhysicalNode::getFieldInfo)
				.map(PhysicalDatabeanFieldInfo::getTableConfiguration)
				.exclude(Objects::isNull)
				.distinct()
				.list();
	}

	public Map<ClientTableEntityPrefixNameWrapper,NodewatchConfiguration> getTableConfigMap(){
		return Scanner.of(getTableConfigurations())
				.toMap(config -> config.nodeNameWrapper);
	}

}

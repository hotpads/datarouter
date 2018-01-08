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
package io.datarouter.storage.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SettingRoot extends SettingNode{

	private final Set<SettingNode> rootNodes = Collections.synchronizedSet(new LinkedHashSet<>());

	public SettingRoot(SettingFinder finder, String name){
		super(finder, name);
		this.rootNodes.add(this);
	}

	public void dependsOn(SettingRoot settingNode){
		rootNodes.add(settingNode);
		settingNode.rootNodes.forEach(rootNodes::add);
	}

	public SettingNode getNode(String nodeName){
		for(SettingNode settingNode : rootNodes){
			if(nodeName.startsWith(settingNode.getName())){
				return settingNode.getNodeByName(nodeName);
			}
		}
		return null;
	}

	public SettingNode getMostRecentAncestorNode(String nodeName){
		for(SettingNode settingNode : rootNodes){
			if(nodeName.startsWith(settingNode.getName())){
				return settingNode.getMostRecentAncestorNodeByName(nodeName);
			}
		}
		return null;
	}

	public List<SettingNode> getDescendants(String nodeName){
		for(SettingNode settingNode : rootNodes){
			if(nodeName.startsWith(settingNode.getName())){
				return settingNode.getDescendanceByName(nodeName);
			}
		}
		return null;
	}

	public List<SettingNode> getRootNodesOrdered(){
		List<SettingNode> list = new ArrayList<>(rootNodes);
		Collections.reverse(list);//TODO make this tree-aware
		return list;
	}

	public Setting<?> getSettingByName(String name){
		SettingNode node = getNode(name.substring(0, name.lastIndexOf(".") + 1));
		if(node == null){
			return null;
		}
		return node.getSettings().get(name);
	}

}

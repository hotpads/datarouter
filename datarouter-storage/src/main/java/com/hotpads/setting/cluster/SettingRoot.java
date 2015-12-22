package com.hotpads.setting.cluster;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.setting.Setting;

public abstract class SettingRoot {

	private List<SettingNode> rootNodes = new ArrayList<>();

	public void add(SettingNode settingNode) {
		rootNodes.add(settingNode);
	}

	public SettingNode getNodeByName(String nodeName) {
		for (SettingNode settingNode : rootNodes) {
			if (nodeName.startsWith(settingNode.getName())) {
				return settingNode.getNodeByName(nodeName);
			}
		}
		return null;
	}

	public List<SettingNode> getDescendanceByName(String nodeName) {
		for (SettingNode settingNode : rootNodes) {
			if (nodeName.startsWith(settingNode.getName())) {
				return settingNode.getDescendanceByName(nodeName);
			}
		}
		return null;
	}

	public List<SettingNode> getRootNodes() {
		return rootNodes;
	}
	
	public Setting<?> getSettingByName(String name){
		SettingNode node = getNodeByName(name.substring(0, name.lastIndexOf(".") + 1));
		if(node == null){
			return null;
		}
		return node.getSettings().get(name);
	}

}

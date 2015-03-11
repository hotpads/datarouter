package com.hotpads.setting.cluster;

import java.util.ArrayList;
import java.util.List;

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

}

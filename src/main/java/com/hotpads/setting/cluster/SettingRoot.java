package com.hotpads.setting.cluster;

import java.util.List;

import com.hotpads.datarouter.util.core.ListTool;

public abstract class SettingRoot {

	private List<SettingNode> rootNodes = ListTool.create();

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

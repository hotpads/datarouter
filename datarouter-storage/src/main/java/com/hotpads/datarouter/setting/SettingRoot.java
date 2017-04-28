package com.hotpads.datarouter.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class SettingRoot extends SettingNode{

	private final LinkedHashSet<SettingNode> rootNodes = new LinkedHashSet<>();

	public SettingRoot(SettingFinder finder, String name, String parentName){
		super(finder, name, parentName);
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

package com.hotpads.job.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import javax.inject.Inject;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class SettingNode{
	
	
	/*********** fields ***********/
	
	private String parentName;
	private String name;
	protected SortedMap<String,SettingNode> children;
	
	protected SortedMap<String,Setting<?>> settings;
	protected ClusterSettingFinder finder;
	
	
	/*********** construct ***********/
	
	@Inject
	public SettingNode(ClusterSettingFinder finder, String name, String parentName){
		this.name = name;
		this.parentName = parentName;
		this.children = Collections.synchronizedSortedMap(MapTool.<String,SettingNode>createTreeMap());
		this.settings = Collections.synchronizedSortedMap(MapTool.<String,Setting<?>>createTreeMap());
		this.finder = finder;
	}
	
	
	/*********** methods ***********/
	
	protected <S extends Setting<?>> S register(S setting){
		settings.put(setting.getName(), setting);
		return setting;
	}
	
	public SettingNode getDescendantByName(String nameParam){
		if(getName().equals(nameParam)){
			return this;
		}
		if(getChildren().containsKey(nameParam)){
			return getChildren().get(nameParam);
		}
		String nextChildShortName = nameParam.substring(getName().length());
		int index = nextChildShortName.indexOf('.');
		String nextChildPath = getName()+nextChildShortName.substring(0, index+1);
		if(getChildren().containsKey(nextChildPath)){
			return getChildren().get(nextChildPath).getDescendantByName(nameParam);
		}
		return null;
	}
	
	public List<SettingNode> getDescendanceByName(String nameParam){
		ArrayList<SettingNode> list = ListTool.createArrayList();
		if(getName().equals(nameParam)){
			list.add(this);
			return list;
		}
		String nextChildShortName = nameParam.substring(getName().length());
		int index = nextChildShortName.indexOf('.');
		String nextChildPath = getName()+nextChildShortName.substring(0, index+1);
		if(getChildren().containsKey(nextChildPath)){
			list.add(this);
			list.addAll(getChildren().get(nextChildPath).getDescendanceByName(nameParam));
		}		
		return list;
	}
	
	public Setting<?> getDescendantSettingByName(String settingNameParam){
		if(getSettings().containsKey(settingNameParam)){
			return getSettings().get(settingNameParam);
		}
		String nextChildShortName = settingNameParam.substring(getName().length());
		int index = nextChildShortName.indexOf('.');
		String nextChildPath = getName()+nextChildShortName.substring(0, index+1);
		if(getChildren().containsKey(nextChildPath)){
			return getChildren().get(nextChildPath).getDescendantSettingByName(settingNameParam);
		}
		return null;
	}
	
	public List<SettingNode> getListChildren(){
		ArrayList<SettingNode> list = ListTool.createArrayList();
		for (String childName : children.keySet()){
			list.add(children.get(childName));
		}
		return list;
	}
	
	public ArrayList<Setting<?>> getListSettings(){
		return ListTool.createArrayList(settings.values());
	}
	
	public String getShortName(){
		String shortName = getName().substring(getParentName().length());
		shortName = shortName.substring(0, shortName.length()-1);
		return shortName;
	}
	
	
	/*********** get/set ***********/
	
	public String getName(){
		return name;
	}
	
	public String getParentName(){
		return parentName;
	}
	
	public SortedMap<String,Setting<?>> getSettings(){
		return settings;
	}
	
	public SortedMap<String,SettingNode> getChildren(){
		return children;
	}
	
}

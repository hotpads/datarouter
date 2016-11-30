package com.hotpads.datarouter.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hotpads.datarouter.setting.cached.impl.BooleanCachedSetting;
import com.hotpads.datarouter.setting.cached.impl.DoubleCachedSetting;
import com.hotpads.datarouter.setting.cached.impl.DurationCachedSetting;
import com.hotpads.datarouter.setting.cached.impl.IntegerCachedSetting;
import com.hotpads.datarouter.setting.cached.impl.LongCachedSetting;
import com.hotpads.datarouter.setting.cached.impl.StringCachedSetting;
import com.hotpads.util.core.Duration;

public abstract class SettingNode {


	/*********** fields ***********/

	private final String parentName;
	private final String name;
	private final SortedMap<String,SettingNode> children;

	private final SortedMap<String,Setting<?>> settings;
	private final SettingFinder finder;

	private final Boolean isGroup;


	/*********** construct ***********/

	public SettingNode(SettingFinder finder, String name, String parentName){
		this(finder, name, parentName, false);
	}

	public SettingNode(SettingFinder finder, String name, String parentName, Boolean isGroup){
		this.name = name;
		this.parentName = parentName;
		this.children = Collections.synchronizedSortedMap(new TreeMap<String,SettingNode>());
		this.settings = Collections.synchronizedSortedMap(new TreeMap<String,Setting<?>>());
		this.finder = finder;
		this.isGroup = isGroup;
	}

	/*********** methods ***********/

	protected <N extends SettingNode> N registerChild(N child){
		if(isGroup){//groups have no children
			throw new RuntimeException("No children allowed for groups");
		}
		children.put(child.getName(), child);
		return child;
	}

	private <S extends Setting<?>> S register(S setting){
		settings.put(setting.getName(), setting);
		return setting;
	}

	public SettingNode getNodeByName(String nameParam){
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
			return getChildren().get(nextChildPath).getNodeByName(nameParam);
		}
		return null;
	}

	public List<SettingNode> getDescendanceByName(String nameParam){
		ArrayList<SettingNode> list = new ArrayList<>();
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
		if(isGroup){//groups have no children
			return null;
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
		ArrayList<SettingNode> list = new ArrayList<>();
		for (String childName : children.keySet()){
			list.add(children.get(childName));
		}
		return list;
	}

	public ArrayList<Setting<?>> getListSettings(){
		return new ArrayList<>(settings.values());
	}

	public String getShortName(){
		String shortName = getName().substring(getParentName().length());
		return shortName.substring(0, shortName.length()-1);
	}

	protected StringCachedSetting registerString(String name, String defaultValue){
		return register(new StringCachedSetting(finder, getName() + name, defaultValue));
	}

	protected BooleanCachedSetting registerBoolean(String name, Boolean defaultValue){
		return register(new BooleanCachedSetting(finder, getName() + name, defaultValue));
	}

	protected IntegerCachedSetting registerInteger(String name, Integer defaultValue){
		return register(new IntegerCachedSetting(finder, getName() + name, defaultValue));
	}

	protected LongCachedSetting registerLong(String name, Long defaultValue){
		return register(new LongCachedSetting(finder, getName() + name, defaultValue));
	}

	protected DoubleCachedSetting registerDouble(String name, Double defaultValue){
		return register(new DoubleCachedSetting(finder, getName() + name, defaultValue));
	}

	protected DurationCachedSetting registerDuration(String name, Duration defaultValue){
		return register(new DurationCachedSetting(finder, getName() + name, defaultValue));
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

	public Boolean isGroup(){
		return isGroup;
	}

}

package com.hotpads.setting.cached;

import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.util.core.ObjectTool;
import com.hotpads.setting.Setting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.util.core.cache.Cached;

//object hierarchy is weird here.  consider using composition
public abstract class CachedSetting<T>
extends Cached<T>
implements Setting<T>{

	protected ClusterSettingFinder finder;
	protected String name;
	protected T defaultValue;
	protected boolean hasCustomValues;
	protected boolean hasRedundantCustomValues;
	
	public CachedSetting(ClusterSettingFinder finder, String name, T defaultValue){
		super(15, TimeUnit.SECONDS);
		this.finder = finder;
		this.name = name;
		this.defaultValue = defaultValue;
	}
	
	/******************* Setting methods *************************/

	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public T getDefaultValue(){
		return defaultValue;
	}
	
	@Override
	public T getValue(){
		return super.get();
	}

	@Override
	public boolean getHasCustomValue(){
		return getValue() != null;
	}

	@Override
	public boolean getHasRedundantCustomValue(){
		return ObjectTool.equals(getDefaultValue(), getValue());
	}
	
}

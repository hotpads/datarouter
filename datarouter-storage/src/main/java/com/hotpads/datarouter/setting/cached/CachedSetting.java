package com.hotpads.datarouter.setting.cached;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.util.core.cache.Cached;

//object hierarchy is weird here.  consider using composition
public abstract class CachedSetting<T>
extends Cached<T>
implements Setting<T>{

	protected final SettingFinder finder;
	protected final String name;
	protected final T defaultValue;
	protected boolean hasCustomValues;
	protected boolean hasRedundantCustomValues;

	public CachedSetting(SettingFinder finder, String name, T defaultValue){
		super(15, TimeUnit.SECONDS);
		this.finder = finder;
		this.name = name;
		this.defaultValue = defaultValue;
	}

	/******************* Setting methods *************************/

	public abstract T parseStringValue(String stringValue);

	@Override
	protected T reload(){
		return finder.getSettingValue(name).map(this::parseStringValue).orElse(defaultValue);
	}

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
		return Objects.equals(getDefaultValue(), getValue());
	}

}

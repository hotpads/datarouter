package com.hotpads.conveyor;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements Conveyor{

	private final String name;
	private final Setting<Boolean> shouldRunSetting;


	public BaseConveyor(String name, Setting<Boolean> shouldRunSetting){
		this.name = name;
		this.shouldRunSetting = shouldRunSetting;
	}


	@Override
	public String getName(){
		return name;
	}

	@Override
	public boolean shouldRun(){
		return !Thread.currentThread().isInterrupted() && shouldRunSetting.getValue();
	}

}

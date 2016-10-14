package com.hotpads.job.trigger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TriggerTracker{

	/******************fields******************/

	protected Map<Class<? extends Job>,TriggerInfo> map;


	/******************constructors******************/

	public TriggerTracker(){
		this.map = Collections.synchronizedMap(new HashMap<Class<? extends Job>,TriggerInfo>());
	}


	/******************methods******************/

	public TriggerInfo get(Class<? extends Job> key){
		return getMap().get(key);
	}

	public void createNewTriggerInfo(Class<? extends Job> key){
		this.put(key, new TriggerInfo());
	}

	public void put(Class<? extends Job> key, TriggerInfo val){
		getMap().put(key, val);
	}


	/******************getters/setters******************/

	public Map<Class<? extends Job>,TriggerInfo> getMap(){
		return map;
	}

	public void setMap(Map<Class<? extends Job>,TriggerInfo> map){
		this.map = map;
	}


}

package com.hotpads.datarouter.config;

public class Configs {

	public static Config slaveOk(){
		return new Config().setSlaveOk(true);
	}
	
	
	public static Config noTimeout(){
		return new Config().setTimeoutMs(Long.MAX_VALUE);
	}


	public static Config insertOrBust(){
		return new Config().setPutMethod(PutMethod.INSERT_OR_BUST);
	}


	public static Config updateOrBust(){
		return new Config().setPutMethod(PutMethod.UPDATE_OR_BUST);
	}


	public static Config insertOrUpdate(){
		return new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE);
	}


	public static Config updateOrInsert(){
		return new Config().setPutMethod(PutMethod.UPDATE_OR_INSERT);
	}


	public static Config merge(){
		return new Config().setPutMethod(PutMethod.MERGE);
	}

	
}

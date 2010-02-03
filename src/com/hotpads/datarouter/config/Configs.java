package com.hotpads.datarouter.config;

public class Configs {

	public static final Config SLAVE_OK = new Config().setSlaveOk(true);

	public static final Config INSERT_OR_BUST = new Config().setPutMethod(PutMethod.INSERT_OR_BUST);
	public static final Config UPDATE_OR_BUST = new Config().setPutMethod(PutMethod.UPDATE_OR_BUST);
	public static final Config INSERT_OR_UPDATE = new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE);
	public static final Config UPDATE_OR_INSERT = new Config().setPutMethod(PutMethod.UPDATE_OR_INSERT);
	public static final Config MERGE = new Config().setPutMethod(PutMethod.MERGE);
	
}

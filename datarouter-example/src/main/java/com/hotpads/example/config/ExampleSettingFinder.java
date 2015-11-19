package com.hotpads.example.config;

import com.hotpads.setting.cluster.MemorySettingFinder;

public class ExampleSettingFinder extends MemorySettingFinder{

	@Override
	protected void configureSettings(){
		// Put your config here like this:
		// settings.put("datarouter.numThreadsForMaxThreadsTest", 500);
	}

}

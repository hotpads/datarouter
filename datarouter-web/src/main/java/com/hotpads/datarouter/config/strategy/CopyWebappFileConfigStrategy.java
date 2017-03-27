package com.hotpads.datarouter.config.strategy;

import com.hotpads.datarouter.app.ApplicationPaths;
import com.hotpads.datarouter.config.configurer.strategy.CopyFileConfigStrategy;

public abstract class CopyWebappFileConfigStrategy extends CopyFileConfigStrategy{

	public CopyWebappFileConfigStrategy(ApplicationPaths applicationPaths, String sourceFileLocation,
			String destinationFileName){
		super(applicationPaths.getResourcesPath() + "/" + sourceFileLocation, destinationFileName);
	}

}

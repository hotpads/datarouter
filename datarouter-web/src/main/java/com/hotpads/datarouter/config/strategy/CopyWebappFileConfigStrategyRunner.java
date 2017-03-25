package com.hotpads.datarouter.config.strategy;

import com.hotpads.datarouter.app.ApplicationPaths;

public abstract class CopyWebappFileConfigStrategyRunner extends CopyFileConfigStrategyRunner{

	public CopyWebappFileConfigStrategyRunner(ApplicationPaths applicationPaths, String sourceFileLocation,
			String destinationFileName){
		super(applicationPaths.getResourcesPath() + "/" + sourceFileLocation, destinationFileName);
	}

}

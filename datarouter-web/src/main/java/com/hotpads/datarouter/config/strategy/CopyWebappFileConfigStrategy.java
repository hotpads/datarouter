package com.hotpads.datarouter.config.strategy;

import com.hotpads.datarouter.app.ApplicationPaths;

public abstract class CopyWebappFileConfigStrategy extends CopyFileConfigStrategy{

	public CopyWebappFileConfigStrategy(ApplicationPaths applicationPaths, String sourceFileLocation,
			String destinationFileName){
		super(applicationPaths.getResourcesPath() + "/" + sourceFileLocation, destinationFileName);
	}

}

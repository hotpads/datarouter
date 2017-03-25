package com.hotpads.datarouter.config.strategy;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.hotpads.datarouter.app.ApplicationPaths;

public class CopyWebappFileConfigStrategyRunner implements ConfigStrategyRunner{
	private static final Logger logger = LoggerFactory.getLogger(CopyWebappFileConfigStrategyRunner.class);

	private final ApplicationPaths applicationPaths;
	private final String sourceFileLocation;
	private final String destinationFileLocation;


	public CopyWebappFileConfigStrategyRunner(ApplicationPaths applicationPaths, String sourceFileLocation,
			String destinationFileLocation){
		this.applicationPaths = applicationPaths;
		this.sourceFileLocation = sourceFileLocation;
		this.destinationFileLocation = destinationFileLocation;
	}


	@Override
	public void configure(Optional<String> configDirectory){
		File sourceFile = new File(applicationPaths.getResourcesPath() + "/" + sourceFileLocation);
		File destinationFile = new File(configDirectory + "/" + destinationFileLocation);
		if(destinationFile.exists()){
			logger.warn("replacing {} with {}", destinationFile.getAbsolutePath(), sourceFile.getAbsolutePath());
		}else{
			logger.warn("creating {} from {}", destinationFile.getAbsolutePath(), sourceFile.getAbsolutePath());
		}
		try{
			Files.copy(sourceFile, destinationFile);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}

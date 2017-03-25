package com.hotpads.datarouter.config.strategy;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public abstract class CopyFileConfigStrategy implements ConfigStrategy{
	private static final Logger logger = LoggerFactory.getLogger(CopyFileConfigStrategy.class);

	private final String sourceFileLocation;
	private final String destinationFileName;


	public CopyFileConfigStrategy(String sourceFileLocation, String destinationFileName){
		this.sourceFileLocation = sourceFileLocation;
		this.destinationFileName = destinationFileName;
	}


	@Override
	public void configure(Optional<String> configDirectory){
		File sourceFile = new File(sourceFileLocation);
		File destinationFile = new File(configDirectory.get() + "/" + destinationFileName);
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

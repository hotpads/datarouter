package com.hotpads.datarouter.config.strategy;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class CopyLocalFileConfigStrategy implements Callable<Void>{
	private static final Logger logger = LoggerFactory.getLogger(CopyLocalFileConfigStrategy.class);

	private final String sourceFileLocation;
	private final String destinationFileLocation;


	public CopyLocalFileConfigStrategy(String sourceFileLocation, String destinationFileLocation){
		this.sourceFileLocation = sourceFileLocation;
		this.destinationFileLocation = destinationFileLocation;
	}


	@Override
	public Void call() throws IOException{
		File sourceFile = new File(sourceFileLocation);
		File destinationFile = new File(destinationFileLocation);
		if(destinationFile.exists()){
			logger.warn("replacing {} with {}", destinationFile.getAbsolutePath(), sourceFile.getAbsolutePath());
		}else{
			logger.warn("creating {} from {}", destinationFile.getAbsolutePath(), sourceFile.getAbsolutePath());
		}
		Files.copy(sourceFile, destinationFile);
		return null;
	}

}

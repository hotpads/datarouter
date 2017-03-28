package com.hotpads.datarouter.config.configurer.strategy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyResourcesFileConfigStrategy implements ConfigStrategy{
	private static final Logger logger = LoggerFactory.getLogger(CopyResourcesFileConfigStrategy.class);

	private final String sourceFileLocation;
	private final String destinationFileName;


	public CopyResourcesFileConfigStrategy(String sourceFileLocation, String destinationFileName){
		this.sourceFileLocation = sourceFileLocation;
		this.destinationFileName = destinationFileName;
	}


	@Override
	public void configure(String configDirectory){
		Path destinationPath = Paths.get(configDirectory + "/" + destinationFileName);
		File destinationFile = destinationPath.toFile();
		if(destinationFile.exists()){
			logger.warn("replacing {} with classpath:{}", destinationFile.getAbsolutePath(), sourceFileLocation);
		}else{
			logger.warn("creating {} from classpath:{}", destinationFile.getAbsolutePath(), sourceFileLocation);
		}
		try(InputStream sourceInputStream = getClass().getResourceAsStream(sourceFileLocation)){
			Files.copy(sourceInputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

}

/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.config.configurer.strategy;

import java.io.File;
import java.io.IOException;

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
	public void configure(String configDirectory){
		File sourceFile = new File(sourceFileLocation);
		File destinationFile = new File(configDirectory + "/" + destinationFileName);
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

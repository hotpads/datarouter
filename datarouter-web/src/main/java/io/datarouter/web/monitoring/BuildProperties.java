/*
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
package io.datarouter.web.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.net.UrlTool;
import io.datarouter.web.app.ApplicationPaths;

@Singleton
public class BuildProperties{
	private static final Logger logger = LoggerFactory.getLogger(BuildProperties.class);

	public static final String FILE_NAME = "build.properties";
	private static final String BUILD_ID = "buildId";

	private final Properties properties = new Properties();

	@Inject
	public BuildProperties(ApplicationPaths applicationPaths){
		URL url = UrlTool.create("file:" + applicationPaths.getResourcesPath() + "/" + FILE_NAME);
		logger.warn("loading build info from {}", url);
		try(InputStream resourceAsStream = url.openStream()){
			properties.load(resourceAsStream);
		}catch(Exception e){
			logger.warn("could not load build info {}", e.toString());
		}
	}

	public BuildProperties(InputStream inputStream){
		try{
			properties.load(inputStream);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public String getBuildId(){
		return properties.getProperty(BUILD_ID);
	}

}

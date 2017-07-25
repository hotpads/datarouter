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
package io.datarouter.inject.guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Stage;

public class GuiceStageFinder{
	private static final Logger logger = LoggerFactory.getLogger(GuiceStageFinder.class);

	private static final String PROPERTY_NAME = "guice.stage";

	public Stage getGuiceStage(){
		String stageString = System.getProperty(PROPERTY_NAME);
		final Stage stage;
		boolean development = Stage.DEVELOPMENT.name().equals(stageString);
		boolean production = Stage.PRODUCTION.name().equals(stageString);
		if(development || production){
			stage = development ? Stage.DEVELOPMENT : Stage.PRODUCTION;
			logger.warn("using Guice Stage {} from JVM arg -D{}={}", stage, PROPERTY_NAME, stageString);
		}else if(stageString != null){
			stage = Stage.PRODUCTION;
			logger.warn("unrecognized JVM arg value -D{}={}, using Guice Stage {}", PROPERTY_NAME, stageString,
					Stage.PRODUCTION);
		}else{
			stage = Stage.PRODUCTION;
			logger.warn("using default Guice Stage={}", Stage.PRODUCTION);
		}
		return stage;
	}

}

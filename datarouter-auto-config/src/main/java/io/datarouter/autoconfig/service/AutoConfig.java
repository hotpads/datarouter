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
package io.datarouter.autoconfig.service;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AutoConfig extends Callable<String>{
	static final Logger logger = LoggerFactory.getLogger(AutoConfig.class);

	static final String NEW_LINE = "\n";
	static final String MARKER = "===============================================================================";

	String getName();
	String configure();

	@Override
	default String call(){
		StringBuilder sb = new StringBuilder();
		sb.append(NEW_LINE);
		sb.append(MARKER).append(NEW_LINE);
		sb.append(getName()).append(NEW_LINE);
		try{
			sb.append(" - ").append(configure()).append(NEW_LINE);
		}catch(Exception e){
			sb.append(" - Error: ").append(e.getMessage());
		}
		sb.append(NEW_LINE);
		logger.warn(sb.toString());
		return sb.toString();
	}

}

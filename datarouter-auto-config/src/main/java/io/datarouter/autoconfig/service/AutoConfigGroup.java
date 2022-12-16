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
package io.datarouter.autoconfig.service;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;

public interface AutoConfigGroup extends Callable<String>, PluginConfigValue<AutoConfigGroup>{
	Logger logger = LoggerFactory.getLogger(AutoConfigGroup.class);

	PluginConfigKey<AutoConfigGroup> KEY = new PluginConfigKey<>("autoConfigGroup", PluginConfigType.CLASS_LIST);

	String NEW_LINE = AutoConfig.NEW_LINE;
	String MARKER = AutoConfig.MARKER;

	String getName();
	List<String> configure();

	@Override
	default String call(){
		StringBuilder sb = new StringBuilder();
		sb.append(NEW_LINE);
		sb.append(MARKER).append(NEW_LINE);
		sb.append(getName()).append(NEW_LINE);
		try{
			configure().forEach(action -> {
				try{
					sb.append(" - ").append(action).append(NEW_LINE);
				}catch(Exception e){
					throw new RuntimeException("Exception while performing action: " + action);
				}
			});
		}catch(Exception e){
			logger.error("Exception while performing autoconfig", e);
			sb.append(" - Exception: ").append(e.toString());
		}
		sb.append(NEW_LINE);
		logger.warn(sb.toString());
		return sb.toString();
	}

	@Override
	default PluginConfigKey<AutoConfigGroup> getKey(){
		return KEY;
	}

}

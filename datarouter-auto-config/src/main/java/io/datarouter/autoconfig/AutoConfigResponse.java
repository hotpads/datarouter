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
package io.datarouter.autoconfig;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoConfigResponse{
	private static final Logger logger = LoggerFactory.getLogger(AutoConfigResponse.class);

	private static final String NEW_LINE = "\n";

	public final List<String> actionsCompleted;
	public final String webappName;

	public AutoConfigResponse(String webappName){
		this.actionsCompleted = new ArrayList<>();
		this.webappName = webappName;
	}

	public AutoConfigResponse log(String str){
		actionsCompleted.add(str);
		return this;
	}

	public String printAutoConfigLog(){
		StringBuilder sb = new StringBuilder();
		sb.append(NEW_LINE);
		sb.append("================================================================================").append(NEW_LINE);
		sb.append(webappName).append(NEW_LINE);
		actionsCompleted.forEach(str -> sb.append(str).append(NEW_LINE));
		sb.append(NEW_LINE);
		logger.warn(sb.toString());
		return sb.toString();
	}

}

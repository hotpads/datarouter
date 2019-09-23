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
package io.datarouter.instrumentation.autoconfig;

import java.util.Collection;

public class ConfigScanResponseTool{

	public static ConfigScanDto buildEmptyResponse(){
		return new ConfigScanDto("", false);
	}

	public static ConfigScanDto buildResponse(String str){
		return new ConfigScanDto("<h4>" + str + "</h4>", true);
	}

	public static ConfigScanDto buildResponse(String description, Collection<String> items){
		StringBuilder sb = new StringBuilder();
		sb.append("<h4>").append(description).append("</h4>");
		sb.append("<ul>");
		items.forEach(item -> sb.append("<li>").append(item).append("</li>"));
		sb.append("</ul>");
		return new ConfigScanDto(sb.toString(), true);
	}

}

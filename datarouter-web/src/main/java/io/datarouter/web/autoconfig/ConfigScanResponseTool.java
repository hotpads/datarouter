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
package io.datarouter.web.autoconfig;

import static j2html.TagCreator.each;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;

import java.util.Collection;

import j2html.tags.specialized.DivTag;

public class ConfigScanResponseTool{

	public static ConfigScanDto buildEmptyResponse(){
		return new ConfigScanDto("", false);
	}

	public static ConfigScanDto buildResponse(String str){
		return new ConfigScanDto(h4(str).renderFormatted(), true);
	}

	public static ConfigScanDto buildResponse(String description, Collection<String> items){
		var h4 = h4(description);
		var ul = ul().with(each(items, tag -> li(tag)));
		return new ConfigScanDto(h4.renderFormatted() + "\n" + ul.render(), true);
	}

	public static ConfigScanDto buildResponse(DivTag containerTag){
		return new ConfigScanDto(containerTag.renderFormatted(), true);
	}

}

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
package io.datarouter.web.js;

import java.util.Map;
import java.util.stream.Collectors;

import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class DatarouterWebJsTool{

	public static ContainerTag makeJsImport(String contextPath, PathNode path){
		return TagCreator.script()
				.withSrc(contextPath + path.toSlashedString());
	}

	public static String buildRawJsObject(Map<String,String> map){
		String result = Scanner.of(map.entrySet())
				.map(entry -> entry.getKey() + ": '" + entry.getValue() + "'")
				.collect(Collectors.joining(",\n", "{", "}"));
		return result;
	}

}

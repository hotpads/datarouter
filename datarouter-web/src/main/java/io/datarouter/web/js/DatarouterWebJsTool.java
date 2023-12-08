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
package io.datarouter.web.js;

import static j2html.TagCreator.script;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import j2html.tags.specialized.ScriptTag;

public class DatarouterWebJsTool{

	public static ScriptTag makeJsImport(String contextPath, PathNode path){
		return script()
				.withSrc(contextPath + path.toSlashedString());
	}

	public static String buildRawJsObject(Map<String,String> map){
		return Scanner.of(map.entrySet())
				.map(entry -> entry.getKey() + ": '" + entry.getValue() + "'")
				.collect(Collectors.joining(",\n", "{", "}"));
	}

	/**
	 * build a JS Object from PathNodes. keys are the last node's value and values are the slashed path plus the prefix
	 * @param nodes the nodes to use
	 * @param pathPrefix optional but must start with a slash and end with no slash if present
	 * @return String that can be used raw in JS
	 */
	public static String buildRawJsPathsObject(Optional<String> pathPrefix, Collection<PathNode> nodes){
		return Scanner.of(nodes)
				.map(node -> {
					String value = node.getValue();
					String path = node.join(pathPrefix.orElse("") + "/", "/", "");
					return value + ": '" + path + "'";
				})
				.collect(Collectors.joining(",\n", "{", "}"));
	}

}

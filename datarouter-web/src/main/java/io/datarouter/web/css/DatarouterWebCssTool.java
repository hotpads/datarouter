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
package io.datarouter.web.css;

import static j2html.TagCreator.link;

import java.util.List;

import io.datarouter.pathnode.PathNode;
import j2html.attributes.Attr;
import j2html.tags.EmptyTag;

public class DatarouterWebCssTool{

	public static EmptyTag<?> makeCssImportTag(String contextPath, PathNode pathNode){
		String path = contextPath + pathNode.toSlashedString();
		return link()
				.attr(Attr.REL, "stylesheet")
				.attr(Attr.HREF, path);
	}

	public static EmptyTag<?>[] makeCssImportTags(String contextPath, List<PathNode> pathNodes){
		return pathNodes.stream()
				.map(pathNode -> makeCssImportTag(contextPath, pathNode))
				.toArray(EmptyTag[]::new);
	}

}

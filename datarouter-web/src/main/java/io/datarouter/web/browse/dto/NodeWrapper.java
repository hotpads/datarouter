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
package io.datarouter.web.browse.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.SortedStorage;
import io.datarouter.util.net.UrlTool;

public class NodeWrapper{

	private final String name;
	private final String className;
	private final int levelsNested;
	private final boolean sorted;

	public NodeWrapper(String name, String className, int levelsNested, boolean sorted){
		this.name = name;
		this.className = className;
		this.levelsNested = levelsNested;
		this.sorted = sorted;
	}

	public String getName(){
		return name;
	}

	public String getClassName(){
		return className;
	}

	public String getIndentHtml(){
		return String.join("", Collections.nCopies(4 * levelsNested, "&nbsp;"));
	}

	public String getUrlEncodedName(){
		return UrlTool.encode(name);
	}

	public boolean isSorted(){
		return sorted;
	}

	public static List<NodeWrapper> getNodeWrappers(Collection<Node<?,?,?>> nodes){
		return nodes.stream()
				.flatMap(node -> addNodeAndChildren(node, 0))
				.collect(Collectors.toList());
	}

	public static Stream<NodeWrapper> addNodeAndChildren(Node<?,?,?> node, int indent){
		boolean sorted = node instanceof SortedStorage;
		NodeWrapper nodeWrapper = new NodeWrapper(node.getName(), node.getClass().getName(), indent, sorted);
		return Stream.concat(Stream.of(nodeWrapper), node.getChildNodes().stream()
				.flatMap(childNode -> addNodeAndChildren(childNode, indent + 1)));
	}

}
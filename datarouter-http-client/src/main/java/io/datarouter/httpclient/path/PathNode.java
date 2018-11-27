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
package io.datarouter.httpclient.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PathNode{

	private final List<PathNode> children = new ArrayList<>();

	protected PathNode parent;
	protected String value;

	public <P extends PathNode> P branch(Supplier<P> childSupplier, String childName){
		P child = childSupplier.get();
		child.parent = this;
		child.value = childName;
		children.add(child);
		return child;
	}

	public PathNode leaf(String childName){
		PathNode child = new PathNode();
		child.parent = this;
		child.value = childName;
		children.add(child);
		return child;
	}

	public List<PathNode> paths(){
		List<PathNode> paths = new ArrayList<>();
		paths.add(this);
		children.stream()
				.map(PathNode::paths)
				.forEach(paths::addAll);
		return paths;
	}

	public String toSlashedString(){
		List<PathNode> ancestry = new ArrayList<>();
		PathNode nextParent = this;
		while(nextParent != null && nextParent.value != null){
			ancestry.add(nextParent);
			nextParent = nextParent.parent;
		}
		Collections.reverse(ancestry);
		return ancestry.stream()
				.map(pathNode -> pathNode.value)
				.collect(Collectors.joining("/", "/", ""));
	}

	//purposefully not usable to avoid unwanted dependencies
	@Override
	public String toString(){
		throw new RuntimeException("PathNode::toString is unusable to avoid unwanted dependencies. PathNode.value="
				+ value);
	}

	public String getValue(){
		return value;
	}

}

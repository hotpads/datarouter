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
package io.datarouter.pathnode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FileSystemNode{

	private final List<FileSystemNode> children = new ArrayList<>();

	protected FileSystemNode parent;
	protected String value;
	protected boolean isFile;

	@SuppressWarnings("unchecked")
	public <P extends FileSystemNode> P directory(Supplier<P> childSupplier, String childName){
		return children.stream()
				.filter(child -> child.value.equals(childName))
				.findAny()
				.map(child -> (P)child)
				.orElseGet(() -> {
					P directoryNode = childSupplier.get();
					directoryNode.parent = this;
					directoryNode.value = childName;
					directoryNode.isFile = false;
					children.add(directoryNode);
					return directoryNode;
				});
	}

	public FileSystemNode file(String childName){
		FileSystemNode fileNode = new FileSystemNode();
		fileNode.parent = this;
		fileNode.value = childName;
		fileNode.isFile = true;
		children.add(fileNode);
		return fileNode;
	}

	/**
	 * a safe way to get the absolute path of the given node with a trailing slash the trailing slash will be omitted if
	 * the leaf node is a file
	 *
	 * @return String representation of the absolute path for the given node with trailing slash if not a file
	 */
	public String toSlashedStringWithTrailingSlash(){
		if(isFile){
			return join("/", "/", "");
		}
		return join("/", "/", "/");
	}

	/**
	 * @return String representation of the absolute path for the given node
	 */
	public String toSlashedString(){
		return join("/", "/", "");
	}

	public String join(String prefix, String delimiter, String suffix){
		return reverse().stream()
				.map(pathNode -> pathNode.value)
				.collect(Collectors.joining(delimiter, prefix, suffix));
	}

	/**
	 * a lazy way to create FS structures will create the underlying directories only, even if the node includes a file
	 *
	 * @return Supplier string representation of the absolute path including the file. Will including a trailing slash,
	 *         but only if leaf node is not a file
	 */
	public Supplier<String> createAndGetSlashedStringWithTrailingSlashSupplier(){
		return () -> {
			createDirectoriesIfNeeded(getPathWithoutFile());
			return toSlashedStringWithTrailingSlash();
		};
	}

	/**
	 * a lazy way to create FS structures will create the underlying directories only, even if the node includes a file
	 *
	 * @return Supplier string representation of the absolute path including the file
	 */
	public Supplier<String> createAndGetSlashedStringSupplier(){
		return () -> {
			createDirectoriesIfNeeded(getPathWithoutFile());
			return toSlashedString();
		};
	}

	public String getValue(){
		return value;
	}

	public boolean isFileSystemNodeFile(){
		return isFile;
	}

	public String getPathWithoutFile(){
		String path = toSlashedString();
		if(isFile){
			path = getStringBeforeLastOccurrence('/', toSlashedString());
		}
		return path;
	}

	private List<FileSystemNode> reverse(){
		List<FileSystemNode> nodes = new ArrayList<>();
		FileSystemNode cursor = this;
		while(cursor != null && cursor.value != null){
			nodes.add(cursor);
			cursor = cursor.parent;
		}
		Collections.reverse(nodes);
		return nodes;
	}

	private void createDirectoriesIfNeeded(String absolutePath){
		Path mainIndexDirectoryPath = Paths.get(absolutePath);
		try{
			Files.createDirectories(mainIndexDirectoryPath);
		}catch(IOException e){
			throw new RuntimeException("Path does not exist and unable to create it", e);
		}
	}

	@Override
	public int hashCode(){
		return toSlashedString().hashCode();
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof FileSystemNode other)){
			return false;
		}
		return isFile == other.isFile && Objects.equals(toSlashedString(), other.toSlashedString());
	}

	// purposefully not usable to avoid unwanted dependencies
	@Override
	public String toString(){
		throw new RuntimeException("FileSystemNode::toString is unusable to avoid unwanted dependencies. "
				+ "FileSystemNode.value=" + value);
	}

	private static String getStringBeforeLastOccurrence(char ch, String sourceString){
		if(sourceString == null){
			return null;
		}
		int index = sourceString.lastIndexOf(Character.toString(ch));
		if(index < 0){
			return "";
		}
		return sourceString.substring(0, index);
	}

}

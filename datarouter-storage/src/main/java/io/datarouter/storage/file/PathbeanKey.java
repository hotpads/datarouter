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
package io.datarouter.storage.file;

import java.nio.file.Path;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.util.Require;

public class PathbeanKey extends BaseRegularPrimaryKey<PathbeanKey>{

	/**
	 * An extension of a path from some other root
	 *  - Expects parent to end with a slash
	 *  - Starts without slash, given that it's appended to the parent
	 *  - If not empty, should end with a slash
	 */
	private String path;
	/**
	 * The last path segment, representing an object or file
	 *  - No starting slash, as the previous path should end with one
	 *  - Cannot contain any slashes
	 */
	private String file;

	public static class FieldKeys{
		public static final StringFieldKey path = new StringFieldKey("path");
		public static final StringFieldKey file = new StringFieldKey("file");
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.path, path),
				new StringField(FieldKeys.file, file));
	}

	public PathbeanKey(){
	}

	public PathbeanKey(String path, String file){
		boolean isValidPath = path == null || isValidPath(path);
		boolean isValidFile = file == null || isValidFile(file);
		if(!isValidPath || !isValidFile){
			String message = String.format("validPath=%s, validFile=%s in [path=%s][file=%s]", isValidPath, isValidFile,
					path, file);
			throw new IllegalArgumentException(message);
		}
		this.path = path;
		this.file = file;
	}

	public static PathbeanKey of(String pathAndFile){
		int lastSlashIndex = pathAndFile.lastIndexOf('/');
		String keyDirectory;
		String keyFile;
		if(lastSlashIndex < 0){// file only
			keyDirectory = "";
			keyFile = pathAndFile;
		}else{
			keyDirectory = pathAndFile.substring(0, lastSlashIndex + 1);
			keyFile = pathAndFile.substring(lastSlashIndex + 1);
		}
		return new PathbeanKey(keyDirectory, keyFile);
	}

	public static PathbeanKey of(Path path){
		Require.greaterThan(path.getNameCount(), 0);
		int nameCount = path.getNameCount();
		String keyPath = nameCount == 1
				? ""
				: path.subpath(0, nameCount - 1) + "/";
		String keyFile = path.getFileName().toString();
		return new PathbeanKey(keyPath, keyFile);
	}

	public static final boolean isValidPath(String path){
		if(path.isEmpty()){
			return true;//empty is ok
		}
		if(path.startsWith("/")){
			return false;
		}
		if(path.contains("//")){
			return false;
		}
		if(!path.endsWith("/")){
			return false;
		}
		return true;
	}

	public static final boolean isValidFile(String file){
		if(file.contains("/")){
			return false;
		}
		if(file.length() == 0){
			return false;
		}
		return true;
	}

	public String getPathAndFile(){
		return path + file;
	}

	public String getPath(){
		return path;
	}

	public String getFile(){
		return file;
	}

	// override default toString() which uses percent encoding that obscures the slashes
	@Override
	public String toString(){
		return getClass().getSimpleName() + "." + getPathAndFile();
	}

}

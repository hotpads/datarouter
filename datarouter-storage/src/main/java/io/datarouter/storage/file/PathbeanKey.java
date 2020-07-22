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
		if(path != null){
			Require.isTrue(isValidPath(path));
		}
		this.path = path;
		if(file != null){
			Require.isTrue(isValidFile(file));
		}
		this.file = file;
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

}

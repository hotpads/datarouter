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
package io.datarouter.filesystem.snapshot.path;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import io.datarouter.util.Require;

@Singleton
public class SnapshotPathsRegistry{

	private final Map<String,SnapshotPaths> byName;

	public SnapshotPathsRegistry(){
		byName = new ConcurrentHashMap<>();

		//register built-in implementations
		register(SnapshotPathsV1.FORMAT, new SnapshotPathsV1());

		//custom implementations can also be registered by calling the public register method
	}

	public void register(String name, SnapshotPaths snapshotPaths){
		Require.notContains(byName.keySet(), name);
		byName.put(name, snapshotPaths);
	}

	public SnapshotPaths getPaths(String name){
		SnapshotPaths paths = byName.get(name);
		if(paths == null){
			String message = String.format("Path implementation not found: %s", name);
			throw new IllegalArgumentException(message);
		}
		return paths;
	}

}

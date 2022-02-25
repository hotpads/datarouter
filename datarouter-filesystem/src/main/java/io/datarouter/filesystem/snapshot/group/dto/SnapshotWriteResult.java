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
package io.datarouter.filesystem.snapshot.group.dto;

import java.util.Optional;

import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.util.Require;

public class SnapshotWriteResult{

	public final SnapshotKey key;
	public final boolean success;
	public final Optional<RootBlock> optRoot;

	private SnapshotWriteResult(SnapshotKey key, boolean success, RootBlock root){
		this.key = key;
		this.success = success;
		this.optRoot = Optional.ofNullable(root);
	}

	public static SnapshotWriteResult success(SnapshotKey key, RootBlock root){
		return new SnapshotWriteResult(key, true, root);
	}

	public static SnapshotWriteResult empty(SnapshotKey key){
		return new SnapshotWriteResult(key, false, null);
	}

	public static SnapshotWriteResult failure(SnapshotKey key){
		return new SnapshotWriteResult(key, false, null);
	}

	public void assertSuccess(){
		Require.isTrue(success);
	}

	public SnapshotKeyAndRoot toSnapshotKeyAndRoot(){
		return new SnapshotKeyAndRoot(key, optRoot.orElseThrow());
	}

}

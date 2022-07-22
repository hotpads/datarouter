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
package io.datarouter.filesystem.snapshot.group.groupops;

import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

public class SnapshotGroupFileReadOps{

	private final String groupId;
	private final Directory idDirectory;
	private final Directory fileDirectory;

	public SnapshotGroupFileReadOps(
			String groupId,
			Directory idDirectory,
			Directory fileDirectory){
		this.groupId = groupId;
		this.idDirectory = idDirectory;
		this.fileDirectory = fileDirectory;
	}

	public Scanner<PathbeanKey> scanSnapshotFilesFromStorage(){
		try{
			return fileDirectory.scanKeys(Subpath.empty());
		}catch(Exception e){
			//filesystem directories likely missing
			//TODO create those directories on startup
			return Scanner.empty();
		}
	}

	public Scanner<SnapshotKey> scanSnapshotKeysFromStorage(){
		try{
			return idDirectory.scanKeys(Subpath.empty())
					.map(PathbeanKey::getFile)
					.map(snapshotId -> new SnapshotKey(groupId, snapshotId));
		}catch(Exception e){
			//filesystem directories likely missing
			//TODO create those directories on startup
			return Scanner.empty();
		}
	}

}

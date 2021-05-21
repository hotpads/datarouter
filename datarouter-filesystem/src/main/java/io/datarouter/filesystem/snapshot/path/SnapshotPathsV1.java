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

import io.datarouter.filesystem.snapshot.storage.block.CacheBlockKey;
import io.datarouter.filesystem.snapshot.storage.file.FileKey;
import io.datarouter.util.string.StringTool;

public class SnapshotPathsV1 implements SnapshotPaths{

	public static final String FORMAT = "pathsV1";

	private static final boolean DIRECTORY_100 = true;// otherwise 10 files per directory. 100 seems to be faster
	// for 100 files per directory
	private static final int[] DIGIT_POSITIONS = new int[]{13, 12, 10, 9, 7, 6, 4, 3, 1, 0};

	@Override
	public String format(){
		return FORMAT;
	}

	@Override
	public String branchFile(FileKey fileKey){
		String levelString = StringTool.pad(Integer.toString(fileKey.level), '0', 3);
		return "branch/" + levelString + "/" + makeFilename(fileKey.fileId);
	}

	@Override
	public String leafFile(FileKey fileKey){
		return "leaf/" + makeFilename(fileKey.fileId);
	}

	@Override
	public String valueFile(FileKey fileKey){
		String columnString = StringTool.pad(Integer.toString(fileKey.column), '0', 3);
		return "value/" + columnString + "/" + makeFilename(fileKey.fileId);
	}

	@Override
	public String branchBlock(CacheBlockKey cacheBlockKey){
		return String.join("/",
				"branch",
				Integer.toString(cacheBlockKey.level),
				Integer.toString(cacheBlockKey.blockId));
	}

	@Override
	public String leafBlock(CacheBlockKey cacheBlockKey){
		return String.join("/",
				"leaf",
				Integer.toString(cacheBlockKey.blockId));
	}

	@Override
	public String valueBlock(CacheBlockKey cacheBlockKey){
		return String.join("/",
				"value",
				Integer.toString(cacheBlockKey.column),
				Integer.toString(cacheBlockKey.blockId));
	}

	private static String makeFilename(int index){
		if(DIRECTORY_100){
			byte[] name = new byte[]{'0', '0', '/', '0', '0', '/', '0', '0', '/', '0', '0', '/', '0', '0'};
			int remaining = index;
			for(int i = 0; remaining > 0 && i < DIGIT_POSITIONS.length; ++i){
				int ascii = '0' + remaining % 10;
				name[DIGIT_POSITIONS[i]] = (byte)ascii;
				remaining /= 10;
			}
			return new String(name);
		}else{
			byte[] name = new byte[19];
			int remaining = index;
			int position = 18;
			while(position >= 0){
				int ascii = '0' + remaining % 10;
				name[position] = (byte)ascii;
				if(position > 0){
					name[position - 1] = '/';
				}
				remaining /= 10;
				position -= 2;
			}
			return new String(name);
		}
	}

}

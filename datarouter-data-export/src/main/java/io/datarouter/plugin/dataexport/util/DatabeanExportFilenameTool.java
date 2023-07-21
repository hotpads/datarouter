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
package io.datarouter.plugin.dataexport.util;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.Subpath;

public class DatabeanExportFilenameTool{

	public static final Subpath META_SUBPATH = new Subpath("meta");
	public static final Subpath DATA_SUBPATH = new Subpath("data");

	private static final String PART_FILE_PREFIX = "part";

	public static boolean isPartFile(PathbeanKey pathbeanKey){
		return pathbeanKey.getFile().startsWith(PART_FILE_PREFIX);
	}

	// prefix the length of the partId to keep sorting while avoiding padding
	public static String makePartFilename(int partId){
		return String.format(
				"%s-%s-%s",
				PART_FILE_PREFIX,
				Integer.toString(partId).length(),
				Integer.toString(partId));
	}

	public static int partId(PathbeanKey pathbeanKey){
		return Scanner.of(pathbeanKey.getFile().split("-"))
				.findLast()
				.map(Integer::valueOf)
				.orElseThrow();
	}

	public static String makeClientAndTableName(PhysicalNode<?,?,?> node){
		return String.join("-", node.getClientId().getName(), node.getFieldInfo().getTableName());
	}

	public record ClientAndTableName(
			String clientName,
			String tableName){
	}

	public static ClientAndTableName parseClientAndTableName(String filename){
		String[] tokens = filename.split("-");
		return new ClientAndTableName(tokens[0], tokens[1]);
	}

}

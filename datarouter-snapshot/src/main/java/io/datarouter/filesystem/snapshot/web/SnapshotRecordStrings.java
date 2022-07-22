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
package io.datarouter.filesystem.snapshot.web;

import java.util.List;

public class SnapshotRecordStrings{

	public final long id;
	public final String key;
	public final String value;
	public final List<String> columnValues;

	public SnapshotRecordStrings(long id, String key, String value, List<String> columnValues){
		this.id = id;
		this.key = key;
		this.value = value;
		this.columnValues = columnValues;
	}

}

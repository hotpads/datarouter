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
package io.datarouter.tasktracker.storage;

import io.datarouter.enums.DatarouterEnumTool;
import io.datarouter.enums.StringEnum;

public enum LongRunningTaskHeartBeatStatus implements StringEnum<LongRunningTaskHeartBeatStatus>{
	STALLED("stalled"),
	WARNING("warning"),
	OK("ok");

	private final String status;

	LongRunningTaskHeartBeatStatus(String status){
		this.status = status;
	}

	@Override
	public String getPersistentString(){
		return status;
	}

	@Override
	public LongRunningTaskHeartBeatStatus fromPersistentString(String str){
		return fromPersistentStringStatic(str);
	}

	public static LongRunningTaskHeartBeatStatus fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

}

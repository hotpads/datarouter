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
package io.datarouter.types;

public class MilliTimeTool{

	public static MilliTime max(MilliTime milliTime1, MilliTime milliTime2){
		if(milliTime1 == null){
			return milliTime2;
		}
		if(milliTime2 == null){
			return milliTime1;
		}
		if(milliTime1.toEpochMilli() > milliTime2.toEpochMilli()){
			return milliTime1;
		}
		return milliTime2;
	}

}

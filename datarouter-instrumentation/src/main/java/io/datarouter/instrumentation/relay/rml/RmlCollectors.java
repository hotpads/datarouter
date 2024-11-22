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
package io.datarouter.instrumentation.relay.rml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class RmlCollectors{

	public static Collector<RmlBlock,?,List<RmlBlock>> joining(
			RmlBlock delimiter){
		return joining(delimiter, null, null);
	}

	public static Collector<RmlBlock,?,List<RmlBlock>> joining(
			RmlBlock delimiter,
			RmlBlock prefix,
			RmlBlock suffix){
		return Collector.<RmlBlock,List<RmlBlock>,List<RmlBlock>>of(
				ArrayList::new,
				(arr, el) -> {
					if(!arr.isEmpty()){
						arr.add(delimiter);
					}
					arr.add(el);
				},
				(arr1, arr2) -> {
					if(arr1.isEmpty()){
						return arr2;
					}
					if(arr2.isEmpty()){
						return arr1;
					}
					arr1.add(delimiter);
					arr1.addAll(arr2);
					return arr1;
				},
				arr -> {
					if(suffix != null){
						arr.add(suffix);
					}
					if(prefix != null){
						arr.add(0, prefix);
					}
					return arr;
				});
	}

}

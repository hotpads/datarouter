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
package io.datarouter.clustersetting;

import java.util.Comparator;

import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.util.lang.ObjectTool;

public class ClusterSettingScopeComparator implements Comparator<ClusterSetting>{

	@Override
	public int compare(ClusterSetting first, ClusterSetting second){
		if(ObjectTool.bothNull(first, second)){
			return 0;
		}
		if(ObjectTool.isOneNullButNotTheOther(first, second)){
			return first == null ? -1 : 1;
		}

		ClusterSettingScope firstScope = first.getScope();
		ClusterSettingScope secondScope = second.getScope();
		if(ObjectTool.bothNull(firstScope, secondScope)){
			return 0;
		}
		if(ObjectTool.isOneNullButNotTheOther(firstScope, secondScope)){
			return firstScope == null ? -1 : 1;
		}

		int difference = firstScope.specificity - secondScope.specificity;
		if(difference == 0){
			return 0;
		}
		return difference < 0 ? -1 : 1;
	}

}

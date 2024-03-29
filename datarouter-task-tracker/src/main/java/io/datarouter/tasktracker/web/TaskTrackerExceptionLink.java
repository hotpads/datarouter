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
package io.datarouter.tasktracker.web;

import io.datarouter.pathnode.PathNode;

public interface TaskTrackerExceptionLink{

	String buildExceptionDetailLink(String exceptionRecordId);

	PathNode getPath();

	String getParamName();

	class NoOpTaskTrackerExceptionLink implements TaskTrackerExceptionLink{

		@Override
		public String buildExceptionDetailLink(String exceptionRecordId){
			return "";
		}

		@Override
		public PathNode getPath(){
			return null;
		}

		@Override
		public String getParamName(){
			return "";
		}

	}

}
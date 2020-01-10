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
package io.datarouter.web.dispatcher;

import javax.servlet.Filter;

public class FilterParams{

	public final boolean isRegex;
	public final String path;
	public final Class<? extends Filter> filterClass;
	public final FilterParamsOrder order;

	public FilterParams(boolean isRegex, String path, Class<? extends Filter> filterClass){
		this.isRegex = isRegex;
		this.path = path;
		this.filterClass = filterClass;
		this.order = FilterParamsOrder.GROUP_100;
	}

	public FilterParams(boolean isRegex, String path, Class<? extends Filter> filterClass, FilterParamsOrder order){
		this.isRegex = isRegex;
		this.path = path;
		this.filterClass = filterClass;
		this.order = order;
	}

	public static enum FilterParamsOrder{
		GROUP_010(10),
		GROUP_020(20),
		GROUP_030(30),
		GROUP_040(40),
		GROUP_100(100), // default
		;

		private final int order;

		FilterParamsOrder(int order){
			this.order = order;
		}

		public int getOrder(){
			return order;
		}

	}

}

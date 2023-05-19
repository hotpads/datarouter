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
package io.datarouter.web.html.indexpager;

public class IndexPagerParamNames{

	public final String paramSort;
	public final String paramPage;
	public final String paramPageSize;

	public IndexPagerParamNames(
			String paramSort,
			String paramPage,
			String paramPageSize){
		this.paramSort = paramSort;
		this.paramPage = paramPage;
		this.paramPageSize = paramPageSize;
	}

	public static class IndexPagerParamNamesBuilder{
		private static final String DEFAULT_PARAM_sort = "sort";
		private static final String DEFAULT_PARAM_page = "page";
		private static final String DEFAULT_PARAM_pageSize = "pageSize";

		private String paramSort = DEFAULT_PARAM_sort;
		private String paramPage = DEFAULT_PARAM_page;
		private String paramPageSize = DEFAULT_PARAM_pageSize;

		public IndexPagerParamNamesBuilder withParamSort(String paramSort){
			this.paramSort = paramSort;
			return this;
		}

		public IndexPagerParamNamesBuilder setParamPage(String paramPage){
			this.paramPage = paramPage;
			return this;
		}

		public IndexPagerParamNamesBuilder setParamPageSize(String paramPageSize){
			this.paramPageSize = paramPageSize;
			return this;
		}

		public IndexPagerParamNames build(){
			return new IndexPagerParamNames(paramSort, paramPage, paramPageSize);
		}
	}

}
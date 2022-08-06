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

import java.util.Map;
import java.util.Optional;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.html.indexpager.BaseNamedScannerPager.RowsAndTotal;

public class IndexPager<T>{

	public final BaseNamedScannerPager<T> namedScannerPager;
	public final IndexPagerParamNames paramNames;
	public final IndexPagerParams params;

	public IndexPager(
			BaseNamedScannerPager<T> namedScannerPager,
			IndexPagerParamNames paramNames,
			IndexPagerParams params){
		this.namedScannerPager = namedScannerPager;
		this.paramNames = paramNames;
		this.params = params;
	}

	public IndexPage<T> makePage(){
		RowsAndTotal<T> rowsAndTotal = namedScannerPager.findRows(params.sort, params.page, params.pageSize);
		return new IndexPage<>(namedScannerPager.getNames(), paramNames, params, rowsAndTotal);
	}

	public static class IndexPagerParamNames{
		public final String paramSort;
		public final String paramPage;
		public final String paramPageSize;

		public IndexPagerParamNames(String paramSort, String paramPage, String paramPageSize){
			this.paramSort = paramSort;
			this.paramPage = paramPage;
			this.paramPageSize = paramPageSize;
		}
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

	public static class IndexPagerParams{
		public final String sort;
		public final long page;
		public final long pageSize;
		public final long offset;

		public IndexPagerParams(String sort, long page, long pageSize){
			this.sort = sort;
			this.page = page;
			this.pageSize = pageSize;
			offset = (page - 1) * pageSize;
		}

		public IndexPagerParams(IndexPagerParamNames paramNames, Map<String,String> params, String defaultSort){
			this(
					Optional.ofNullable(params.get(paramNames.paramSort))
							.map(StringTool::nullIfEmpty)
							.orElse(defaultSort),
					Optional.ofNullable(params.get(paramNames.paramPage))
							.map(Integer::valueOf)
							.orElse(1),
					Optional.ofNullable(params.get(paramNames.paramPageSize))
							.map(Integer::valueOf)
							.orElse(100));
		}

	}

	public static class IndexPageLink{
		public final String text;
		public final String href;

		public IndexPageLink(
				String path,
				IndexPagerParamNames paramNames,
				IndexPagerParams params,
				String text,
				long linkPage){
			this.text = text;
			this.href = makeHref(path, paramNames, params, linkPage);
		}

		private static String makeHref(
				String path,
				IndexPagerParamNames paramNames,
				IndexPagerParams params,
				long page){
			var href = new URIBuilder()
					.setPath(path)
					.addParameter(paramNames.paramSort, params.sort)
					.addParameter(paramNames.paramPageSize, params.pageSize + "")
					.addParameter(paramNames.paramPage, page + "");
			return href.toString();
		}
	}

}

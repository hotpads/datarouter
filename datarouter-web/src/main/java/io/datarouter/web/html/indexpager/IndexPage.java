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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.web.html.indexpager.BaseNamedScannerPager.RowsAndTotal;
import io.datarouter.web.html.indexpager.IndexPagerParamNames.IndexPagerParamNamesBuilder;

public class IndexPage<T>{

	public final List<String> sortOptions;
	public final IndexPagerParamNames pagerParamNames;
	public final List<String> retainedParamNames;
	public final IndexPagerParams params;
	public final List<T> rows;
	public final long fromRow;
	public final long toRow;
	public final Optional<Long> totalRows;
	public final Long previousPage;
	public final Long nextPage;
	public final Optional<Long> totalPages;

	public IndexPage(
			List<String> sortOptions,
			IndexPagerParamNames pagerParamNames,
			List<String> retainedParamNames,
			IndexPagerParams params,
			RowsAndTotal<T> rowsAndTotal){
		this.sortOptions = sortOptions;
		this.pagerParamNames = pagerParamNames;
		this.retainedParamNames = retainedParamNames;
		this.params = params;
		this.rows = rowsAndTotal.rows;
		this.fromRow = params.offset + 1;
		this.toRow = params.offset + rowsAndTotal.rows.size();
		this.totalRows = rowsAndTotal.totalRows;
		this.previousPage = params.page == 1 ? null : params.page - 1;
		//TODO could overscan by one row to determine if next page exists
		if(totalRows.isEmpty() || toRow < totalRows.orElseThrow()){
			this.nextPage = params.page + 1;
		}else{
			this.nextPage = null;
		}
		this.totalPages = totalRows.map(total -> total / params.pageSize + 1);
	}

	public static class IndexPageBuilder<P,T>{
		public final BaseNamedScannerPager<P,T> namedScannerPager;
		public IndexPagerParamNames pagerParamNames = new IndexPagerParamNamesBuilder().build();
		public int defaultPageSize = 100;
		public List<String> retainedParamNames = new ArrayList<>();

		public IndexPageBuilder(BaseNamedScannerPager<P,T> namedScannerPager){
			this.namedScannerPager = namedScannerPager;
		}

		public IndexPageBuilder<P,T> withDefaultPageSize(int defaultPageSize){
			this.defaultPageSize = defaultPageSize;
			return this;
		}

		public IndexPageBuilder<P,T> withParamNames(IndexPagerParamNames paramNames){
			this.pagerParamNames = paramNames;
			return this;
		}

		/**
		 * Application params that should be included in links to other pages.
		 */
		public IndexPageBuilder<P,T> retainParam(String paramName){
			retainedParamNames.add(paramName);
			return this;
		}

		public IndexPageBuilder<P,T> retainParams(String... paramNames){
			Arrays.asList(paramNames).forEach(this::retainParam);
			return this;
		}

		public IndexPage<T> build(P filterParams, Map<String,String> requestParams){
			var pagerParams = new IndexPagerParams(
					pagerParamNames,
					retainedParamNames,
					requestParams,
					namedScannerPager.getFirstName(),
					defaultPageSize);
			var pager = new IndexPager<>(
					namedScannerPager,
					pagerParamNames,
					retainedParamNames,
					pagerParams);
			return pager.makePage(filterParams);
		}

		public IndexPage<T> build(Map<String,String> requestParams){
			return build(null, requestParams);
		}

	}
}

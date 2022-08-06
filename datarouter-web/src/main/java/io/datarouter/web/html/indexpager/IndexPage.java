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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.web.html.indexpager.BaseNamedScannerPager.RowsAndTotal;
import io.datarouter.web.html.indexpager.IndexPager.IndexPagerParamNames;
import io.datarouter.web.html.indexpager.IndexPager.IndexPagerParamNamesBuilder;
import io.datarouter.web.html.indexpager.IndexPager.IndexPagerParams;

public class IndexPage<T>{

	public final List<String> sortOptions;
	public final IndexPagerParamNames paramNames;
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
			IndexPagerParamNames paramNames,
			IndexPagerParams params,
			RowsAndTotal<T> rowsAndTotal){
		this.sortOptions = sortOptions;
		this.paramNames = paramNames;
		this.params = params;
		this.rows = rowsAndTotal.rows;
		this.fromRow = params.offset + 1;
		this.toRow = params.offset + rowsAndTotal.rows.size();
		this.totalRows = rowsAndTotal.totalRows;
		this.previousPage = params.page == 1 ? null : params.page - 1;
		//TODO could overscan by one row to determine if next page exists
		this.nextPage = params.page + 1;
		this.totalPages = totalRows.map(total -> total / params.pageSize + 1);
	}

	public static class IndexPageBuilder<T>{
		public final BaseNamedScannerPager<T> namedScannerPager;
		public IndexPagerParamNames paramNames = new IndexPagerParamNamesBuilder().build();

		public IndexPageBuilder(BaseNamedScannerPager<T> namedScannerPager){
			this.namedScannerPager = namedScannerPager;
		}

		public IndexPageBuilder<T> withParamNames(IndexPagerParamNames paramNames){
			this.paramNames = paramNames;
			return this;
		}

		public IndexPage<T> build(Map<String,String> paramByName){
			var params = new IndexPagerParams(paramNames, paramByName, namedScannerPager.getFirstName());
			var pager = new IndexPager<>(namedScannerPager, paramNames, params);
			return pager.makePage();
		}

	}
}
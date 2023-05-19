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

import io.datarouter.web.html.indexpager.BaseNamedScannerPager.RowsAndTotal;

public class IndexPager<P,T>{

	public final BaseNamedScannerPager<P,T> namedScannerPager;
	public final IndexPagerParamNames pagerParamNames;
	public final List<String> retainedParamNames;
	public final IndexPagerParams params;

	public IndexPager(
			BaseNamedScannerPager<P,T> namedScannerPager,
			IndexPagerParamNames pagerParamNames,
			List<String> retainedParamNames,
			IndexPagerParams params){
		this.namedScannerPager = namedScannerPager;
		this.pagerParamNames = pagerParamNames;
		this.retainedParamNames = retainedParamNames;
		this.params = params;
	}

	public IndexPage<T> makePage(P scannerCreatorParams){
		RowsAndTotal<T> rowsAndTotal = namedScannerPager.findRows(
				scannerCreatorParams,
				params.sort,
				params.page,
				params.pageSize);
		return new IndexPage<>(
				namedScannerPager.getNames(),
				pagerParamNames,
				retainedParamNames,
				params,
				rowsAndTotal);
	}

}

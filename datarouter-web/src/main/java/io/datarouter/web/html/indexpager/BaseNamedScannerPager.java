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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;

public abstract class BaseNamedScannerPager<P,T>{

	private final Map<String,ScannerPagerSortOption<P,T>> options = new LinkedHashMap<>();

	/**
	 * Scans only enough rows to find the offset and fill the page.
	 */
	public void add(String name, Function<P,Scanner<T>> paramsToScanner){
		options.put(name, new ScannerPagerSortOption<>(name, paramsToScanner, false));
	}

	/**
	 * Drains the entire scanner, adding a total row count and the ability to skip to the last page.
	 *
	 * Warning: this is dangerous for large scanners as all data is loaded into memory.
	 */
	public void addWithTotal(String name, Function<P,Scanner<T>> paramsToScanner){
		options.put(name, new ScannerPagerSortOption<>(name, paramsToScanner, true));
	}

	public List<String> getNames(){
		return new ArrayList<>(options.keySet());
	}

	public String getFirstName(){
		return options.keySet().iterator().next();
	}

	public RowsAndTotal<T> findRows(P scannerCreatorParams, String name, long page, long pageSize){
		ScannerPagerSortOption<P,T> sortOption = options.get(name);
		Scanner<T> scanner = sortOption.scannerCreator.apply(scannerCreatorParams);
		long offset = (page - 1) * pageSize;
		if(sortOption.collectAll){
			List<T> allRows = scanner.list();
			List<T> rows = Scanner.of(allRows)
					.skip(offset)
					.limit(pageSize)
					.list();
			return new RowsAndTotal<>(rows, allRows.size());
		}
		List<T> rows = scanner
				.skip(offset)
				.limit(pageSize)
				.list();
		return new RowsAndTotal<>(rows);
	}

	public static class ScannerPagerSortOption<P,T>{
		public final String displayName;
		//TODO add more url-friendly name
		public final Function<P,Scanner<T>> scannerCreator;
		public final boolean collectAll;

		public ScannerPagerSortOption(
				String displayName,
				Function<P,Scanner<T>> scannerCreator,
				boolean collectAll){
			this.displayName = displayName;
			this.scannerCreator = scannerCreator;
			this.collectAll = collectAll;
		}
	}

	public static class RowsAndTotal<T>{
		public final List<T> rows;
		public final Optional<Long> totalRows;

		public RowsAndTotal(List<T> rows, long totalRows){
			this.rows = rows;
			this.totalRows = Optional.of(totalRows);
		}


		public RowsAndTotal(List<T> rows){
			this.rows = rows;
			this.totalRows = Optional.empty();
		}
	}
}

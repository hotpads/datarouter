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
package io.datarouter.web.html.pager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.BaseHtmlFormField;
import io.datarouter.web.html.form.HtmlFormButton;
import io.datarouter.web.html.form.HtmlFormCheckbox;
import io.datarouter.web.html.form.HtmlFormSelect;
import io.datarouter.web.html.form.HtmlFormText;

/**
 * Paging utility for situations where all data can be loaded into memory for each page.  We're therefore able to
 * sort on any dimension and calculate total page count.
 */
public class MemoryPager<T>{

	public static final String P_pageSize = "pageSize";
	public static final String P_page = "page";
	public static final String P_sort = "sort";
	public static final String P_reverse = "reverse";

	public final List<BaseHtmlFormField> filterFields;
	public final MemorySorter<T> memorySorter;
	public final String path;
	public final Map<String,String> requiredParams;
	public final int pageSize;
	public final int page;
	public final String sort;
	public final boolean reverse;
	private final int offset;

	public MemoryPager(
			List<BaseHtmlFormField> filterFields,
			MemorySorter<T> sorter,
			String path,
			Map<String,String> requiredParams,
			int pageSize,
			int page,
			String sort,
			boolean reverse){
		this.filterFields = filterFields;
		this.memorySorter = sorter != null ? sorter : new MemorySorter<>();
		this.path = path;
		this.requiredParams = requiredParams;
		this.pageSize = pageSize;
		this.page = page;
		this.sort = sort;
		this.reverse = reverse;
		this.offset = pageSize * (page - 1);
	}

	public MemoryPager(
			List<BaseHtmlFormField> filterFields,
			MemorySorter<T> sorter,
			String path,
			Map<String,String> requiredParams,
			Params params,
			int defaultPageSize){
		this(filterFields,
				sorter,
				path,
				requiredParams,
				params.optionalInteger(MemoryPager.P_pageSize).orElse(defaultPageSize),
				params.optionalInteger(MemoryPager.P_page).orElse(1),
				params.optional(MemoryPager.P_sort).orElse(null),
				params.optional(MemoryPager.P_reverse).orElse("").equals("on"));
	}

	public Page<T> collect(Scanner<T> scanner){
		List<T> allRows = memorySorter.apply(scanner, sort, reverse).list();
		List<T> pageRows = allRows.stream()
				.skip(offset)
				.limit(pageSize)
				.collect(Collectors.toList());
		return new Page<>(this, filterFields, path, pageRows, allRows.size());
	}

	public static class Page<T>{

		public final String path;
		public final Map<String,String> requiredParams;
		public final List<BaseHtmlFormField> filterFields;
		public final Map<String,String> sortDisplayByValue;
		public final String sort;
		public final boolean reverse;
		public final int pageSize;
		public final int page;
		public final List<T> rows;
		public final int numRows;
		public final int totalRows;
		public final int totalPages;
		public final int from;
		public final int to;
		public final Integer previousPage;
		public final Integer nextPage;

		public Page(
				MemoryPager<T> pager,
				List<BaseHtmlFormField> filterFields,
				String path,
				List<T> rows,
				int totalRows){
			this.filterFields = filterFields;
			this.path = path;
			this.requiredParams = pager.requiredParams;
			this.sortDisplayByValue = pager.memorySorter.getDisplayByValue();
			this.sort = pager.sort;
			this.reverse = pager.reverse;
			this.pageSize = pager.pageSize;
			this.page = pager.page;
			this.rows = rows;
			this.numRows = rows.size();
			this.totalRows = totalRows;
			this.totalPages = totalRows / pager.pageSize + 1;
			this.from = pager.offset + 1;
			this.to = from - 1 + rows.size();
			this.previousPage = pager.page == 1 ? null : pager.page - 1;
			this.nextPage = to == totalRows ? null : pager.page + 1;
		}

		public HtmlForm makeHtmlForm(){
			var fieldSort = new HtmlFormSelect()
					.withDisplay("Sort")
					.withName(MemoryPager.P_sort)
					.withDisplayByValue(sortDisplayByValue)
					.withSelected(sort);
			var fieldReverse = new HtmlFormCheckbox()
					.withDisplay("Reverse")
					.withName(MemoryPager.P_reverse)
					.withChecked(reverse);
			var fieldPage = new HtmlFormText()
					.withDisplay("Page")
					.withName(MemoryPager.P_page)
					.withValue(page + "");
			var fieldPageSize = new HtmlFormText()
					.withDisplay("Page Size")
					.withName(MemoryPager.P_pageSize)
					.withValue(pageSize + "");
			var fieldSubmit = new HtmlFormButton()
					.withDisplay("Submit");

			var sortFields = new ArrayList<BaseHtmlFormField>();
			if(!sortDisplayByValue.isEmpty()){
				sortFields.add(fieldSort);
				sortFields.add(fieldReverse);//can't easily reverse without a comparator
			}
			sortFields.add(fieldPage);
			sortFields.add(fieldPageSize);
			sortFields.add(fieldSubmit);
			return new HtmlForm()
					.addFields(filterFields)
					.addFields(sortFields);
		}

		public List<PageLink> getLinks(){
			Optional<PageLink> first = page == 1
					? Optional.empty()
					: Optional.of(new PageLink(path, requiredParams, "first", sort, pageSize, 1, reverse));
			Optional<PageLink> previous = previousPage == null
					? Optional.empty()
					: Optional.of(new PageLink(path, requiredParams, "previous", sort, pageSize,
					previousPage.intValue(), reverse));
			Optional<PageLink> next = nextPage == null
					? Optional.empty()
					: Optional.of(new PageLink(path, requiredParams, "next", sort, pageSize, nextPage.intValue(),
					reverse));
			Optional<PageLink> last = nextPage == null
					? Optional.empty()
					: Optional.of(new PageLink(path, requiredParams, "last", sort, pageSize, totalPages, reverse));
			return Stream.of(first, previous, next, last)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toList());
		}

	}

	public static class PageLink{

		public final String path;
		public final Map<String,String> requiredParams;
		public final String text;
		public final String href;

		public PageLink(String path, Map<String,String> requiredParams, String text, String sort, int pageSize,
				int page, boolean reverse){
			this.path = path;
			this.requiredParams = requiredParams;
			this.text = text;
			this.href = makeHref(path, requiredParams, sort, pageSize, page, reverse);
		}

		public static String makeHref(String path, Map<String,String> requiredParams, String sort, int pageSize,
				int page, boolean reverse){
			var href = new URIBuilder()
					.setPath(path)
					.addParameter(P_sort, sort)
					.addParameter(P_pageSize, pageSize + "")
					.addParameter(P_page, page + "");
			requiredParams.forEach(href::addParameter);
			if(reverse){
				href.addParameter(P_reverse, "on");
			}
			return href.toString();
		}
	}

}

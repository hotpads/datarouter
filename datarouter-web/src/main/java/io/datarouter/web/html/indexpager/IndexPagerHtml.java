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
import java.util.function.Function;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.html.form.BaseHtmlFormField;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormHiddenField;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.form.HtmlFormSelect;
import io.datarouter.web.html.form.HtmlFormText;

public class IndexPagerHtml{

	public static <T> HtmlForm makeForm(IndexPage<T> indexPage){
		Map<String,String> displayByValue = Scanner.of(indexPage.sortOptions)
				.toMapSupplied(Function.identity(), LinkedHashMap::new);
		var fieldSort = new HtmlFormSelect()
				.withLabel("Sort")
				.withName(indexPage.pagerParamNames.paramSort)
				.withDisplayByValue(displayByValue)
				.withSelected(indexPage.params.sort)
				.withSubmitOnChange();
		var fieldPage = new HtmlFormText()
				.withLabel("Page")
				.withName(indexPage.pagerParamNames.paramPage)
				.withValue(indexPage.params.page + "")
				.withSubmitOnChange();
		var fieldPageSize = new HtmlFormText()
				.withLabel("Page Size")
				.withName(indexPage.pagerParamNames.paramPageSize)
				.withValue(indexPage.params.pageSize + "")
				.withSubmitOnChange();

		var sortFields = new ArrayList<BaseHtmlFormField<?>>();
		if(displayByValue.size() > 1){
			sortFields.add(fieldSort);
		}
		if(indexPage.totalPages.isPresent() && indexPage.totalPages.orElseThrow() > 1){
			sortFields.add(fieldPage);
			sortFields.add(fieldPageSize);
		}
		List<HtmlFormHiddenField> hiddenFields = Scanner.of(indexPage.params.retainedParams)
				.map(retainedParam -> new HtmlFormHiddenField(retainedParam.name(), retainedParam.value()))
				.list();
		return new HtmlForm(HtmlFormMethod.GET)
				.addFields(sortFields)
				.addHiddenFields(hiddenFields);
	}

	public static List<IndexPageLink> makeLinks(String path, IndexPage<?> indexPage){
		List<IndexPageLink> links = new ArrayList<>();
		if(indexPage.params.page > 1){
			links.add(new IndexPageLink(
					path,
					indexPage.pagerParamNames,
					indexPage.params,
					"first",
					1));
		}
		if(indexPage.previousPage != null){
			links.add(new IndexPageLink(
					path,
					indexPage.pagerParamNames,
					indexPage.params,
					"previous",
					indexPage.previousPage));
		}
		if(indexPage.nextPage != null){
			links.add(new IndexPageLink(
					path,
					indexPage.pagerParamNames,
					indexPage.params,
					"next",
					indexPage.nextPage));
		}
		if(indexPage.totalRows.isPresent() && indexPage.nextPage != null){
			links.add(new IndexPageLink(
					path,
					indexPage.pagerParamNames,
					indexPage.params,
					"last",
					indexPage.totalPages.get()));
		}
		return links;
	}

}

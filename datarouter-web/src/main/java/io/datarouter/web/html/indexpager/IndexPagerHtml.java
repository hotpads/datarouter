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
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.BaseHtmlFormField;
import io.datarouter.web.html.indexpager.IndexPager.IndexPageLink;
import io.datarouter.web.html.form.HtmlFormButton;
import io.datarouter.web.html.form.HtmlFormSelect;
import io.datarouter.web.html.form.HtmlFormText;

public class IndexPagerHtml{

	public static <T> HtmlForm makeForm(IndexPage<T> indexPage){
		Map<String,String> displayByValue = Scanner.of(indexPage.sortOptions)
				.toMapSupplied(Function.identity(), LinkedHashMap::new);
		var fieldSort = new HtmlFormSelect()
				.withDisplay("Sort")
				.withName(indexPage.paramNames.paramSort)
				.withDisplayByValue(displayByValue)
				.withSelected(indexPage.params.sort);
		var fieldPage = new HtmlFormText()
				.withDisplay("Page")
				.withName(indexPage.paramNames.paramPage)
				.withValue(indexPage.params.page + "");
		var fieldPageSize = new HtmlFormText()
				.withDisplay("Page Size")
				.withName(indexPage.paramNames.paramPageSize)
				.withValue(indexPage.params.pageSize + "");
		var fieldSubmit = new HtmlFormButton()
				.withDisplay("Submit");

		var sortFields = new ArrayList<BaseHtmlFormField>();
		if(!displayByValue.isEmpty()){
			sortFields.add(fieldSort);
		}
		sortFields.add(fieldPage);
		sortFields.add(fieldPageSize);
		sortFields.add(fieldSubmit);
		return new HtmlForm()
				.addFields(sortFields);
	}

	public static List<IndexPageLink> makeLinks(String path, IndexPage<?> indexPage){
		List<IndexPageLink> links = new ArrayList<>();
		if(indexPage.params.page > 1){
			links.add(new IndexPageLink(
					path,
					indexPage.paramNames,
					indexPage.params,
					"first",
					1));
		}
		if(indexPage.previousPage != null){
			links.add(new IndexPageLink(
					path,
					indexPage.paramNames,
					indexPage.params,
					"previous",
					indexPage.previousPage));
		}
		if(indexPage.nextPage != null){
			links.add(new IndexPageLink(
					path,
					indexPage.paramNames,
					indexPage.params,
					"next",
					indexPage.nextPage));
		}
		if(indexPage.totalRows.isPresent() && indexPage.nextPage != null){
			links.add(new IndexPageLink(
					path,
					indexPage.paramNames,
					indexPage.params,
					"last",
					indexPage.totalPages.get()));
		}
		return links;
	}

}

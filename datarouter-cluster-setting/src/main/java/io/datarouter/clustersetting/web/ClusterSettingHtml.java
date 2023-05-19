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
package io.datarouter.clustersetting.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.td;

import io.datarouter.clustersetting.ClusterSettingOverrideSuggestion;
import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.config.DatarouterClusterSettingPlugin;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;

public class ClusterSettingHtml{

	public static String makeTitle(String titleSuffix){
		return String.format("%s - %s", DatarouterClusterSettingPlugin.NAME, titleSuffix);
	}

	public static DivTag makeHeader(String title, String subtitle){
		return div(
				h3(title),
				div(subtitle))
				.withClass("mt-3");
	}

	public static DivTag legendDiv(){
		var legend = new J2HtmlLegendTable()
					.withClass("table table-sm border");
		ClusterSettingValidity.stream()
				.forEach(validity -> legend.withEntry(
						validity.persistentString,
						validity.description,
						validity.color));
		return div(legend.build())
				.withStyle("width:600px;");
	}

	public static String overrideSuggestionsTableClass(ClusterSettingOverrideSuggestion suggestion){
		return switch(suggestion){
			case DELETE -> "table-danger";
			case MOVE_TO_CODE -> "table-warning";
			case NOTHING -> "table-default";
		};
	}

	public static TdTag makeLimitedLengthLinkCell(String value, String href){
		int limit = 15;
		String indicator = "...";
		if(value == null || value.length() < limit){
			return td(value);
		}
		String limitedValue = value.substring(0, limit - indicator.length());
		var link = a(limitedValue + indicator)
				.withTitle(value)
				.withHref(href);
		return td(link);
	}

}

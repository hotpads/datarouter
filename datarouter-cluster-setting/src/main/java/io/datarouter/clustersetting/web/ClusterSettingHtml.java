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

import io.datarouter.clustersetting.config.DatarouterClusterSettingPlugin;
import io.datarouter.clustersetting.enums.ClusterSettingOverrideSuggestion;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseEmailLinks;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideViewHandler.ClusterSettingOverrideViewLinks;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingHtml{

	@Inject
	private ClusterSettingOverrideViewLinks overrideViewLinks;
	@Inject
	private ClusterSettingBrowseEmailLinks browseEmailLinks;

	public String makeTitle(String titleSuffix){
		return String.format("%s - %s", DatarouterClusterSettingPlugin.NAME, titleSuffix);
	}

	public DivTag makeHeader(String title, String subtitle){
		return div(
				h3(title),
				div(subtitle))
				.withClass("mt-3");
	}

	public String overrideSuggestionsTableClass(ClusterSettingOverrideSuggestion suggestion){
		return switch(suggestion){
			case DELETE -> "table-danger";
			case MOVE_TO_CODE -> "table-warning";
			case NOTHING -> "table-default";
		};
	}

	public TdTag makeLimitedLengthLinkCell(String value, String href){
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

	public ATag makeBrowseSettingLink(String settingName){
		String href = browseEmailLinks.fromEmail(settingName);
		return a(settingName)
				.withHref(href)
				.withStyle("text-decoration:none;");
	}

	public ATag makeOverridePrefixSettingLink(String settingName){
		String href = overrideViewLinks.view(settingName);
		return a(settingName)
				.withHref(href)
				.withStyle("text-decoration:none;");
	}

}

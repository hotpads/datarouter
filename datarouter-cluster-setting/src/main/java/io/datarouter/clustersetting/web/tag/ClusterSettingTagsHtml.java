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
package io.datarouter.clustersetting.web.tag;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.pre;

import java.util.HashSet;
import java.util.Set;

import io.datarouter.storage.setting.DatarouterSettingTagType;
import io.datarouter.storage.setting.cached.CachedClusterSettingTags;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingTagsHtml{

	@Inject
	private CachedClusterSettingTags cachedClusterSettingTags;

	public DivTag makeEnabledTagsDiv(){
		Set<String> enabledTagNames = new HashSet<>(cachedClusterSettingTags.readTagNames());
		var enabledTagsDiv = div(h5("Enabled tags"));
		DatarouterSettingTagType.scanPersistentStrings()
				.sort()
				.map(availableTagName -> makeTagCheckboxForm(
						availableTagName,
						enabledTagNames.contains(availableTagName)))
				.forEach(enabledTagsDiv::with);
		return enabledTagsDiv;
	}

	public DivTag makeTagCheckboxForm(String tagName, boolean checked){
		var form = new HtmlForm()
				.withMethod("GET");
		form.addHiddenField(ClusterSettingTagsHandler.P_tagName, tagName);
		form.addCheckboxField()
				.withDisplay(tagName)
				.withName(ClusterSettingTagsHandler.P_tagEnabled)
				.withChecked(checked)
				.withSubmitOnChange();
		return div(Bootstrap4FormHtml.render(form, true));
	}

	public DivTag makeTtlDiv(){
		return div(
				h5("Note that values are cached"),
				div(String.format(
						"Changes should take effect within %s seconds.",
						CachedClusterSettingTags.getCacheTtl().toSeconds())));
	}

	public DivTag makeFilesystemPathDiv(){
		return div(
				h5("Config file location"),
				div(pre(CachedClusterSettingTags.getConfigFilePath())));
	}

}

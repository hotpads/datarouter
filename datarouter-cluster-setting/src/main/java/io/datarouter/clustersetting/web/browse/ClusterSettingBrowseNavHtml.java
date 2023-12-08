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
package io.datarouter.clustersetting.web.browse;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.li;
import static j2html.TagCreator.span;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;

import java.util.List;
import java.util.Optional;

import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseHandlerParams;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseLinks;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseSettingNodeHtml.ClusterSettingBrowseSettingNodeHtmlFactory;
import io.datarouter.clustersetting.web.browse.ClusterSettingHierarchy.HierarchyNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.LiTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public record ClusterSettingBrowseNavHtml(
		ClusterSettingBrowseLinks browseLinks,
		ClusterSettingHierarchy hierarchy,
		ClusterSettingBrowseSettingNodeHtml settingNodeHtml,
		ClusterSettingBrowseHandlerParams params){

	private static final int NAV_TREE_WIDTH = 300;
	private static final String LIST_STYLE = "list-style-type:none;margin:0;padding:0;";

	public DivTag makeBodyDiv(){
		Optional<HierarchyNode> optHierarchy = hierarchy.root().filter(params.partialName);
		if(optHierarchy.isEmpty()){
			String message = String.format("No settings matching '%s'", params.partialName.orElseThrow());
			return div(h5(message));
		}
		HierarchyNode filteredHierarchy = optHierarchy.orElseThrow();
		Optional<HierarchyNode> optSelectedSetting = findSelectedSetting(filteredHierarchy);
		Optional<String> optNodeLocation = params.location
				.map(location -> location.substring(0, location.lastIndexOf('.') + 1));
		HierarchyNode selectedNode = getLocation(filteredHierarchy, optNodeLocation);
		HierarchyNode selectedCategory = getSelectedCategory(filteredHierarchy, selectedNode);
		return div(
				makeCountsDiv(hierarchy.root(), filteredHierarchy)
						.withClass("ml-3 mt-2"),
				makeCategoryTabsDiv(filteredHierarchy, selectedCategory)
						.withClass("mt-2"),
				makeBodyUnderTabsDiv(selectedCategory, selectedNode, optSelectedSetting));
	}

	public DivTag makeCountsDiv(
			HierarchyNode unfilteredHierarchy,
			HierarchyNode filteredHierarchy){
		long total = unfilteredHierarchy.countDescendentSettings();
		long filtered = filteredHierarchy.countDescendentSettings();
		boolean isFiltered = filtered < total;
		String message = isFiltered
				? String.format(
						"Found %s of %s total settings with name like '%s'",
						NumberFormatter.addCommas(filtered),
						NumberFormatter.addCommas(total),
						params.partialName.orElseThrow())
				: String.format(
						"%s total settings",
						NumberFormatter.addCommas(total));
		return div(message);
	}

	public DivTag makeCategoryTabsDiv(
			HierarchyNode filteredHierarchy,
			HierarchyNode selectedCategoryHierarchy){
		NavTabs navTabs = new NavTabs();
		filteredHierarchy.scanChildren()
				.forEach(category -> navTabs.add(new NavTab(
						category.name(),
						browseLinks.all(
								new ClusterSettingBrowseHandlerParams()
										.withLocation(getFirstLocation(category).name())
										.withOptPartialName(params.partialName)),
						category.name().equals(selectedCategoryHierarchy.name()))));
		return div(Bootstrap4NavTabsHtml.render(navTabs));
	}

	public DivTag makeBodyUnderTabsDiv(
			HierarchyNode categoryHierarchy,
			HierarchyNode selectedNode,
			Optional<HierarchyNode> optSelectedSetting){
		var navTreeDiv = makeCategoryTreeDiv(categoryHierarchy, selectedNode);
		String location = optSelectedSetting
				.map(HierarchyNode::name)
				.orElse(selectedNode.name());
		var settingsDiv = settingNodeHtml.makeSettingsDiv(
				selectedNode,
				settingsToDisplay(selectedNode, optSelectedSetting),
				location);
		var table = table(tr(
				td(navTreeDiv).withStyle(String.format("vertical-align:top;width:%spx;", NAV_TREE_WIDTH)),
				td(settingsDiv).withStyle("vertical-align:top;")))
				.withStyle("width:100%;");
		return div(table);
	}

	public DivTag makeCategoryTreeDiv(
			HierarchyNode categoryHierarchy,
			HierarchyNode selectedHierarchy){
		var categoryUl = ul()
				.withStyle(LIST_STYLE);
		categoryHierarchy.scanChildren()
				.include(HierarchyNode::isSettingRootOrNode)
				.forEach(node -> categoryUl.with(makeSettingNode(
						node,
						selectedHierarchy)));
		return div(categoryUl)
				.withClass("card-body bg-light border-0 mt-0 p-2");
	}

	public LiTag makeSettingNode(
			HierarchyNode nodeHierarchy,
			HierarchyNode selectedNode){
		var nodeLi = li(makeSettingNodeLiContent(
				nodeHierarchy,
				selectedNode))
				.withStyle(LIST_STYLE);
		List<HierarchyNode> childNodes = nodeHierarchy.scanChildren()
				.include(HierarchyNode::isSettingRootOrNode)
				.list();
		if(!childNodes.isEmpty()){
			var ul = ul()
					.withStyle(LIST_STYLE);
			Scanner.of(childNodes)
					.map(childHierarchy -> makeSettingNode(
							childHierarchy,
							selectedNode))
					.forEach(ul::with);
			nodeLi.with(ul);
		}
		return nodeLi;
	}

	private DivTag makeSettingNodeLiContent(
			HierarchyNode nodeHierarchy,
			HierarchyNode selectedNode){
		int indent = nodeHierarchy.level() - 1;
		var levelSpan = span(StringTool.repeat("-", indent))
				.withStyle("color:gray;");
		boolean selected = nodeHierarchy.name().equals(selectedNode.name());
		var linkText = selected
				? b(nodeHierarchy.shortName())
				: text(nodeHierarchy.shortName());
		var link = a(linkText)
				.withHref(browseLinks.all(
						new ClusterSettingBrowseHandlerParams()
								.withLocation(nodeHierarchy.name())
								.withOptPartialName(params.partialName)));
		var countSpan = span(String.format(" (%s)", nodeHierarchy.countChildSettings()))
				.withStyle("color:gray;font-size:.85em;");
		String textIndentStyle = String.format("text-indent:%spx;", indent * 16);
		return div(
				levelSpan,
				link,
				countSpan)
				.withStyle(textIndentStyle);
	}

	/*----------- helper -------------*/

	private HierarchyNode getLocation(HierarchyNode filteredHierarchy, Optional<String> optLocation){
		return optLocation
				.map(location -> filteredHierarchy.scanDescendents()
						.include(node -> node.name().equals(location))
						.findFirst()
						.orElseGet(() -> getFirstLocation(filteredHierarchy)))
				.orElseGet(() -> getFirstLocation(filteredHierarchy));
	}

	private HierarchyNode getFirstLocation(HierarchyNode parent){
		return parent.scanDescendents()
				.include(HierarchyNode::isSettingRootOrNode)
				.include(HierarchyNode::hasChildSettings)
				.findFirst()
				.orElseThrow();
	}

	private HierarchyNode getSelectedCategory(
			HierarchyNode filteredHierarchy,
			HierarchyNode selectedNode){
		return filteredHierarchy.scanChildren()
				.include(child -> child.scanDescendents()
						.anyMatch(descendent -> descendent.name().equals(selectedNode.name())))
				.findFirst()
				.orElseThrow();
	}

	private Optional<HierarchyNode> findSelectedSetting(HierarchyNode filteredHierarchy){
		return params.location.flatMap(location -> filteredHierarchy.scanDescendents()
				.include(HierarchyNode::isSetting)
				.include(node -> node.name().equals(location))
				.findFirst());
	}

	private List<? extends CachedSetting<?>> settingsToDisplay(
			HierarchyNode selectedNode,
			Optional<HierarchyNode> optSelectedSetting){
		return optSelectedSetting.isPresent()
				? List.of(optSelectedSetting
						.map(HierarchyNode::setting)
						.orElseThrow())
				: selectedNode.scanChildren()
						.include(HierarchyNode::isSetting)
						.map(HierarchyNode::setting)
						.list();
	}

	@Singleton
	public static class ClusterSettingBrowseNavHtmlFactory{
		@Inject
		private ClusterSettingBrowseLinks browseLinks;
		@Inject
		private ClusterSettingHierarchy hierarchy;
		@Inject
		private ClusterSettingBrowseSettingNodeHtmlFactory settingNodeHtmlFactory;

		public ClusterSettingBrowseNavHtml create(ClusterSettingBrowseHandlerParams params){
			return new ClusterSettingBrowseNavHtml(
					browseLinks,
					hierarchy,
					settingNodeHtmlFactory.create(params),
					params);
		}
	}

}

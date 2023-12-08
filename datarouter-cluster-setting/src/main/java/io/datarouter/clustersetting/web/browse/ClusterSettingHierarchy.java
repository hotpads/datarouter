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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.Setting;
import io.datarouter.storage.setting.SettingCategory.SimpleSettingCategory;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.SettingRoot.SettingRootFinder;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * There are 4 levels of navigation for settings: Category -> Setting Root -> Setting Node -> Setting.
 * This wraps them into a tree to facilitate searching and displaying them.
 */
@Singleton
public class ClusterSettingHierarchy{

	private static final String ROOT_NAME = "hierarchy";

	private final SettingRootFinder settingRootFinder;
	private final HierarchyNode hierarchyRoot;
	private final SortedSet<String> settingNamesSorted;

	@Inject
	public ClusterSettingHierarchy(SettingRootFinder settingRootFinder){
		this.settingRootFinder = settingRootFinder;
		hierarchyRoot = makeHierarchyRoot();
		settingNamesSorted = hierarchyRoot.scanThisAndDescendents()
				.include(HierarchyNode::isSetting)
				.map(HierarchyNode::name)
				.collect(TreeSet::new);
	}

	public HierarchyNode root(){
		return hierarchyRoot;
	}

	public SortedSet<String> settingNamesSorted(){
		return settingNamesSorted;
	}

	private HierarchyNode makeHierarchyRoot(){
		return Scanner.of(settingRootFinder.getRootNodesByCategory().keySet())
				.sort(Comparator.comparing(SimpleSettingCategory::getDisplay))
				.map(this::makeCategory)
				.listTo(nodes -> new HierarchyNode(
						HierarchyNodeType.HIERARCHY_ROOT,
						ROOT_NAME,
						nodes,
						null,
						null,
						null));
	}

	private HierarchyNode makeCategory(SimpleSettingCategory category){
		return Scanner.of(settingRootFinder.getRootNodesByCategory().get(category))
				.sort(Comparator.comparing(SettingRoot::getName))
				.map(this::makeNode)
				.listTo(nodes -> new HierarchyNode(
						HierarchyNodeType.CATEGORY,
						category.getDisplay(),
						nodes,
						category,
						null,
						null));
	}

	private HierarchyNode makeNode(SettingNode node){
		HierarchyNodeType type = node.isRoot() ? HierarchyNodeType.SETTING_ROOT : HierarchyNodeType.SETTING_NODE;
		List<HierarchyNode> childNodes = Scanner.of(node.getChildren().values())
				.sort(Comparator.comparing(SettingNode::getName))
				.map(this::makeNode)
				.list();
		List<HierarchyNode> childSettings = Scanner.of(node.getListSettings())
				.sort(Comparator.comparing(Setting::getName))
				.map(this::makeSetting)
				.list();
		List<HierarchyNode> children = Scanner.concat(childNodes, childSettings).list();
		return new HierarchyNode(
				type,
				node.getName(),
				children,
				null,
				node,
				null);
	}

	private HierarchyNode makeSetting(CachedSetting<?> setting){
		return new HierarchyNode(
				HierarchyNodeType.SETTING,
				setting.getName(),
				List.of(),
				null,
				null,
				setting);
	}

	public enum HierarchyNodeType{
		HIERARCHY_ROOT,
		CATEGORY,
		SETTING_ROOT,
		SETTING_NODE,
		SETTING;
	}

	public record HierarchyNode(
			HierarchyNodeType type,
			String name,
			String lowercaseName,
			List<String> nameTokens,
			String shortName,
			int level,
			List<HierarchyNode> children,
			SimpleSettingCategory category,
			SettingNode node,
			CachedSetting<?> setting){

		public HierarchyNode(
				HierarchyNodeType type,
				String name,
				List<HierarchyNode> children,
				SimpleSettingCategory category,
				SettingNode node,
				CachedSetting<?> setting){
			this(type,
					name,
					name.toLowerCase(),
					//TODO use ClusterSettingLocation for parsing
					Scanner.of(name.split("\\.")).list(),
					Scanner.of(name.split("\\.")).findLast().orElseThrow(),
					Scanner.of(name.split("\\.")).countInt(),
					children,
					category,
					node,
					setting);
		}

		/**
		 * Returns a copy of the tree with non-matching values pruned.
		 * Only filters on setting names, not category/node names.
		 */
		public Optional<HierarchyNode> filter(Optional<String> optPartialName){
			if(optPartialName.isEmpty()){
				return Optional.of(this);
			}
			if(isSetting()){
				return lowercaseName.contains(optPartialName.orElseThrow().toLowerCase())
						? Optional.of(this)
						: Optional.empty();
			}
			List<HierarchyNode> matchingChildren = Scanner.of(children)
					.concatOpt(child -> child.filter(optPartialName))
					.list();
			return matchingChildren.isEmpty()
					? Optional.empty()
					: Optional.of(new HierarchyNode(
							type,
							name,
							matchingChildren,
							category,
							node,
							setting));
		}

		public boolean isCategory(){
			return type == HierarchyNodeType.CATEGORY;
		}

		public boolean isSettingRoot(){
			return type == HierarchyNodeType.SETTING_ROOT;
		}

		public boolean isSettingNode(){
			return type == HierarchyNodeType.SETTING_NODE;
		}

		public boolean isSettingRootOrNode(){
			return isSettingRoot() || isSettingNode();
		}

		public boolean isSetting(){
			return type == HierarchyNodeType.SETTING;
		}

		public Scanner<HierarchyNode> scanChildren(){
			return Scanner.of(children);
		}

		public Scanner<HierarchyNode> scanThisAndDescendents(){
			return ObjectScanner.of(this)
					.append(Scanner.of(children)
							.concat(HierarchyNode::scanThisAndDescendents));
		}

		public Scanner<HierarchyNode> scanDescendents(){
			return Scanner.of(children)
					.concat(HierarchyNode::scanThisAndDescendents);
		}

		public boolean hasChildSettingNodes(){
			return scanChildren()
					.include(HierarchyNode::isSettingNode)
					.hasAny();
		}

		public boolean hasChildSettings(){
			return scanChildren()
					.include(HierarchyNode::isSetting)
					.hasAny();
		}

		public long countChildSettings(){
			return scanChildren()
					.include(HierarchyNode::isSetting)
					.count();
		}

		public long countDescendentSettings(){
			return scanThisAndDescendents()
					.include(HierarchyNode::isSetting)
					.count();
		}

		public String nodeName(){
			return name.substring(0, name.lastIndexOf('.'));
		}

		public Optional<CachedSetting<?>> findSetting(String name){
			return scanThisAndDescendents()
					.include(node -> node.name().equals(name))
					.findFirst()
					.map(HierarchyNode::setting);
		}

	}

}